package com.example.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

object PhoneAuthHelper {
    private const val TAG = "PhoneAuth"

    fun toAsciiDigits(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            when (ch) {
                '০' -> sb.append('0')
                '১' -> sb.append('1')
                '২' -> sb.append('2')
                '৩' -> sb.append('3')
                '৪' -> sb.append('4')
                '৫' -> sb.append('5')
                '৬' -> sb.append('6')
                '৭' -> sb.append('7')
                '৮' -> sb.append('8')
                '৯' -> sb.append('9')
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Normalizes Bangladeshi mobile numbers (`01XXXXXXXXX`, `8801XXXXXXXXX`, `+8801XXXXXXXXX`)
     * or international E.164 numbers (`+<digits>`) for Firebase Phone Auth.
     * Returns null if the input is not a valid mobile number.
     */
    fun normalizeToE164(rawInput: String): String? {
        val cleaned = toAsciiDigits(rawInput)
            .replace(" ", "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")
            .trim()
        if (cleaned.isEmpty()) return null

        val e164 = when {
            cleaned.startsWith("+8801") -> cleaned
            cleaned.startsWith("8801") -> "+$cleaned"
            cleaned.startsWith("01") -> "+88$cleaned"
            cleaned.startsWith("+") -> cleaned
            else -> return null
        }

        if (e164.startsWith("+880")) {
            val bdRegex = Regex("^\\+8801[3-9]\\d{8}$")
            return if (bdRegex.matches(e164)) e164 else null
        }

        val intlRegex = Regex("^\\+[1-9]\\d{7,14}$")
        return if (intlRegex.matches(e164)) e164 else null
    }

    fun isValidPhoneForProfile(rawInput: String): Boolean {
        val ascii = toAsciiDigits(rawInput).replace(" ", "").replace("-", "").trim()
        if (ascii.length !in 6..20) return false
        if (ascii.startsWith("01") || ascii.startsWith("+880") || ascii.startsWith("8801")) {
            return normalizeToE164(ascii) != null
        }
        return ascii.count { it.isDigit() } >= 6
    }

    fun formatDisplayPhone(rawInput: String): String {
        val normalized = normalizeToE164(rawInput)
        return normalized ?: toAsciiDigits(rawInput).trim()
    }

    fun sendOtp(
        context: Context,
        rawPhone: String,
        resendToken: PhoneAuthProvider.ForceResendingToken?,
        scope: CoroutineScope,
        onCodeSent: (verificationId: String, token: PhoneAuthProvider.ForceResendingToken, e164Phone: String) -> Unit,
        onAutoVerified: () -> Unit,
        onError: (message: String, isProviderDisabled: Boolean) -> Unit
    ) {
        val e164Phone = normalizeToE164(rawPhone)
        if (e164Phone == null) {
            onError("সঠিক বাংলাদেশি মোবাইল নম্বর দিন (যেমন: 01712345678 বা +8801712345678)।", false)
            return
        }
        val activity = context.findActivity()
        if (activity == null) {
            onError("অ্যাক্টিভিটি পাওয়া যায়নি। আবার চেষ্টা করুন।", false)
            return
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                scope.launch {
                    try {
                        Firebase.auth.signInWithCredential(credential).await()
                        onAutoVerified()
                    } catch (e: Exception) {
                        Log.e(TAG, "Auto phone sign-in failed", e)
                        onError(e.localizedMessage ?: "স্বয়ংক্রিয় যাচাই সম্পন্ন করা যায়নি।", false)
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "Phone verification failed: ${e.message}", e)
                val authCode = (e as? FirebaseAuthException)?.errorCode
                val providerDisabled = authCode == "ERROR_OPERATION_NOT_ALLOWED" ||
                    e.message?.contains("operation-not-allowed", ignoreCase = true) == true ||
                    e.message?.contains("identitytoolkit", ignoreCase = true) == true
                val friendlyMsg = when {
                    providerDisabled ->
                        "Firebase Console-এ Phone Sign-In চালু করা নেই। আপনার নম্বরটি রেখেই নিচের Google বাটন দিয়ে নিরাপদ নিবন্ধন সম্পন্ন করতে পারেন, অথবা Firebase Console থেকে Phone অপশন চালু করুন।"
                    e is FirebaseTooManyRequestsException ->
                        "অতিরিক্ত অনুরোধ করা হয়েছে। কিছুক্ষণ পর আবার চেষ্টা করুন।"
                    authCode == "ERROR_INVALID_PHONE_NUMBER" ->
                        "মোবাইল নম্বরটি সঠিক নয়। +880 সহ বা 01XXXXXXXXX ফরম্যাটে দিন।"
                    else ->
                        e.localizedMessage ?: "OTP পাঠানো যায়নি। নেটওয়ার্ক সংযোগ যাচাই করুন।"
                }
                onError(friendlyMsg, providerDisabled)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                onCodeSent(verificationId, token, e164Phone)
            }
        }

        val builder = PhoneAuthOptions.newBuilder(Firebase.auth)
            .setPhoneNumber(e164Phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)

        if (resendToken != null) {
            builder.setForceResendingToken(resendToken)
        }

        PhoneAuthProvider.verifyPhoneNumber(builder.build())
    }

    fun verifyOtp(
        verificationId: String,
        otpCode: String,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanCode = toAsciiDigits(otpCode).trim()
        if (cleanCode.length != 6 || !cleanCode.all { it.isDigit() }) {
            onError("৬ সংখ্যার সঠিক OTP কোডটি লিখুন।")
            return
        }
        scope.launch {
            try {
                val credential = PhoneAuthProvider.getCredential(verificationId, cleanCode)
                Firebase.auth.signInWithCredential(credential).await()
                onSuccess()
            } catch (e: FirebaseAuthException) {
                Log.w(TAG, "OTP verification failed: ${e.errorCode}", e)
                val msg = when (e.errorCode) {
                    "ERROR_INVALID_VERIFICATION_CODE" -> "ভুল OTP কোড দেওয়া হয়েছে। আবার যাচাই করুন।"
                    "ERROR_SESSION_EXPIRED" -> "OTP কোডের মেয়াদ শেষ হয়েছে। অনুগ্রহ করে নতুন কোড পাঠান।"
                    else -> e.localizedMessage ?: "OTP যাচাই করা যায়নি।"
                }
                onError(msg)
            } catch (e: Exception) {
                Log.e(TAG, "OTP sign-in error", e)
                onError(e.localizedMessage ?: "OTP যাচাই করা যায়নি।")
            }
        }
    }
}
