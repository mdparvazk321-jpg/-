const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const FARMER_UID = "farmer_rahim_123";
const SELLER_UID = "seller_karim_456";
const OTHER_UID = "other_user_789";
const ADMIN_UID = "admin_parvaz_001";
const ADMIN_EMAIL = "mdparvazk321@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastTime(msAgo = 5000) {
  return new Date(Date.now() - msAgo);
}

function futureTime(msAhead = 60000) {
  return new Date(Date.now() + msAhead);
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("products").where("status", "==", "APPROVED").get());
  await assertFails(unauthDb.collection("users").doc(FARMER_UID).get());
  await assertFails(unauthDb.collection("orders").where("buyerId", "==", FARMER_UID).get());
  await assertFails(unauthDb.collection("complaints").where("userId", "==", FARMER_UID).get());
});

test("User profile: owner create/update works; PII blocked for other users; self-verification blocked", async () => {
  const farmerDb = testEnv.authenticatedContext(FARMER_UID).firestore();
  const otherDb = testEnv.authenticatedContext(OTHER_UID).firestore();
  const now = pastTime();

  // Valid Farmer profile creation
  await assertSucceeds(
    farmerDb.collection("users").doc(FARMER_UID).set({
      userId: FARMER_UID,
      name: "মো. রহিম উদ্দিন",
      phone: "01711223344",
      district: "বগুড়া",
      role: "FARMER",
      shopName: "",
      shopAddress: "",
      nidOrLicense: "",
      sellerVerified: false,
      accountStatus: "ACTIVE",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Reject self-verification privilege escalation
  await assertFails(
    otherDb.collection("users").doc(OTHER_UID).set({
      userId: OTHER_UID,
      name: "প্রতারক",
      phone: "01811223344",
      district: "ঢাকা",
      role: "SELLER",
      shopName: "ফেক শপ",
      shopAddress: "ঢাকা",
      nidOrLicense: "123456",
      sellerVerified: true,
      accountStatus: "ACTIVE",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Reject cross-user PII read and unfiltered users list
  await assertFails(otherDb.collection("users").doc(FARMER_UID).get());
  await assertFails(otherDb.collection("users").get());

  // Valid partial update by owner
  await assertSucceeds(
    farmerDb.collection("users").doc(FARMER_UID).update({
      district: "রাজশাহী",
      updatedAt: pastTime(1000),
    })
  );

  // Reject owner trying to flip sellerVerified
  await assertFails(
    farmerDb.collection("users").doc(FARMER_UID).update({
      sellerVerified: true,
      updatedAt: pastTime(500),
    })
  );
});

test("Admin email spoofing blocked when email_verified is false; verified admin can verify seller", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("users").doc(SELLER_UID).set({
      userId: SELLER_UID,
      name: "করিম এগ্রো",
      phone: "01911223344",
      district: "যশোর",
      role: "SELLER",
      shopName: "করিম কৃষি ভান্ডার",
      shopAddress: "যশোর সদর",
      nidOrLicense: "TL-99881",
      sellerVerified: false,
      accountStatus: "ACTIVE",
      createdAt: pastTime(10000),
      updatedAt: pastTime(10000),
    });
  });

  // Spoofed unverified admin email must fail
  const spoofDb = testEnv.authenticatedContext("spoof_uid", {
    email: ADMIN_EMAIL,
    email_verified: false,
  }).firestore();
  await assertFails(spoofDb.collection("users").get());
  await assertFails(
    spoofDb.collection("users").doc(SELLER_UID).update({
      sellerVerified: true,
      updatedAt: pastTime(500),
    })
  );

  // Verified admin succeeds
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();
  await assertSucceeds(adminDb.collection("users").get());
  await assertSucceeds(
    adminDb.collection("users").doc(SELLER_UID).update({
      sellerVerified: true,
      updatedAt: pastTime(500),
    })
  );
});

test("Products: seller create/update, query alignment, shadow field rejection, and admin removal", async () => {
  const sellerDb = testEnv.authenticatedContext(SELLER_UID).firestore();
  const farmerDb = testEnv.authenticatedContext(FARMER_UID).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();
  const now = pastTime();

  // Valid seller product create
  await assertSucceeds(
    sellerDb.collection("products").doc("prod_roundup").set({
      sellerId: SELLER_UID,
      shopName: "সবুজ বাংলা কৃষি ভান্ডার",
      sellerVerified: false,
      name: "রাউন্ডআপ ৪৮০ এসএল",
      category: "আগাছানাশক",
      price: 850,
      originalPrice: 950,
      discountPercent: 10,
      unit: "১ লিটার",
      stock: 40,
      description: "অনুমোদিত আগাছানাশক",
      safetyGuide: "অনুমোদিত মাত্রায় ব্যবহার করুন ও শিশুদের নাগালের বাইরে রাখুন।",
      approvalCode: "AP-BD-480",
      imageKey: "herbicide",
      imageBase64: "",
      badge: "বেস্ট সেলার",
      status: "APPROVED",
      featured: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Shadow update with ghost field rejected
  await assertFails(
    sellerDb.collection("products").doc("prod_roundup").update({
      price: 820,
      ghostField: "hack",
      updatedAt: pastTime(500),
    })
  );

  // Buyer aligned query succeeds; unfiltered query fails
  await assertSucceeds(
    farmerDb.collection("products").where("status", "==", "APPROVED").get()
  );
  await assertFails(farmerDb.collection("products").get());

  // Seller aligned query succeeds
  await assertSucceeds(
    sellerDb.collection("products").where("sellerId", "==", SELLER_UID).get()
  );

  // Admin can remove prohibited product; seller cannot re-approve removed product
  await assertSucceeds(
    adminDb.collection("products").doc("prod_roundup").update({
      status: "REMOVED",
      updatedAt: pastTime(300),
    })
  );
  await assertFails(
    sellerDb.collection("products").doc("prod_roundup").update({
      price: 800,
      updatedAt: pastTime(100),
    })
  );
});

test("Cart, Favorites, and Notifications: strictly isolated to owner", async () => {
  const farmerDb = testEnv.authenticatedContext(FARMER_UID).firestore();
  const otherDb = testEnv.authenticatedContext(OTHER_UID).firestore();
  const now = pastTime();

  await assertSucceeds(
    farmerDb.collection("users").doc(FARMER_UID).collection("cart").doc("prod_1").set({
      userId: FARMER_UID,
      productId: "prod_1",
      productName: "ইউরিয়া সার",
      shopName: "কৃষক বন্ধু",
      sellerId: SELLER_UID,
      unit: "৫০ কেজি",
      price: 320,
      quantity: 2,
      imageKey: "urea",
      imageBase64: "",
      updatedAt: now,
    })
  );
  await assertSucceeds(
    farmerDb.collection("users").doc(FARMER_UID).collection("favorites").doc("prod_1").set({
      userId: FARMER_UID,
      productId: "prod_1",
      productName: "ইউরিয়া সার",
      category: "সার",
      price: 320,
      unit: "৫০ কেজি",
      shopName: "কৃষক বন্ধু",
      sellerVerified: true,
      imageKey: "urea",
      imageBase64: "",
      createdAt: now,
    })
  );
  await assertSucceeds(
    farmerDb.collection("users").doc(FARMER_UID).collection("notifications").doc("notif_1").set({
      userId: FARMER_UID,
      title: "অর্ডার নিশ্চিত",
      body: "আপনার অর্ডারটি গ্রহণ করা হয়েছে।",
      type: "ORDER",
      readStatus: false,
      createdAt: now,
    })
  );

  // Cross-user access denied
  await assertFails(otherDb.collection("users").doc(FARMER_UID).collection("cart").get());
  await assertFails(otherDb.collection("users").doc(FARMER_UID).collection("favorites").get());
  await assertFails(otherDb.collection("users").doc(FARMER_UID).collection("notifications").get());
});

test("Orders: buyer creates, seller advances status, terminal DELIVERED locked, query alignment", async () => {
  const farmerDb = testEnv.authenticatedContext(FARMER_UID).firestore();
  const sellerDb = testEnv.authenticatedContext(SELLER_UID).firestore();
  const otherDb = testEnv.authenticatedContext(OTHER_UID).firestore();
  const now = pastTime();

  await assertSucceeds(
    farmerDb.collection("orders").doc("order_1").set({
      buyerId: FARMER_UID,
      buyerName: "মো. রহিম",
      buyerPhone: "01711223344",
      deliveryAddress: "গ্রাম: শেরপুর, বগুড়া",
      sellerId: SELLER_UID,
      shopName: "করিম কৃষি ভান্ডার",
      items: [{ productId: "prod_1", name: "ইউরিয়া সার", price: 320, quantity: 2, unit: "৫০ কেজি" }],
      totalAmount: 640,
      paymentMethod: "ক্যাশ অন ডেলিভারি",
      status: "PENDING",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Tampered future timestamp rejected
  await assertFails(
    farmerDb.collection("orders").doc("order_future").set({
      buyerId: FARMER_UID,
      buyerName: "মো. রহিম",
      buyerPhone: "01711223344",
      deliveryAddress: "বগুড়া",
      sellerId: SELLER_UID,
      shopName: "করিম কৃষি ভান্ডার",
      items: [{ productId: "prod_1", name: "ইউরিয়া সার", price: 320, quantity: 1, unit: "৫০ কেজি" }],
      totalAmount: 320,
      paymentMethod: "ক্যাশ অন ডেলিভারি",
      status: "PENDING",
      createdAt: futureTime(),
      updatedAt: futureTime(),
    })
  );

  // Buyer query & Seller query succeed; other user & unfiltered fail
  await assertSucceeds(farmerDb.collection("orders").where("buyerId", "==", FARMER_UID).get());
  await assertSucceeds(sellerDb.collection("orders").where("sellerId", "==", SELLER_UID).get());
  await assertFails(otherDb.collection("orders").doc("order_1").get());
  await assertFails(farmerDb.collection("orders").get());

  // Seller updates status to DELIVERED, then terminal state locks further seller edits
  await assertSucceeds(
    sellerDb.collection("orders").doc("order_1").update({
      status: "DELIVERED",
      updatedAt: pastTime(500),
    })
  );
  await assertFails(
    sellerDb.collection("orders").doc("order_1").update({
      status: "CANCELLED",
      updatedAt: pastTime(100),
    })
  );
});

test("Complaints: user submits, admin resolves, user cannot self-resolve", async () => {
  const farmerDb = testEnv.authenticatedContext(FARMER_UID).firestore();
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();
  const now = pastTime();

  await assertSucceeds(
    farmerDb.collection("complaints").doc("comp_1").set({
      userId: FARMER_UID,
      userName: "মো. রহিম",
      userPhone: "01711223344",
      subject: "পণ্য ডেলিভারি বিলম্ব",
      message: "তিন দিন হয়ে গেছে এখনো ডেলিভারি পাইনি।",
      targetType: "ORDER",
      targetId: "order_1",
      status: "OPEN",
      adminNote: "",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Non-admin cannot resolve complaint
  await assertFails(
    farmerDb.collection("complaints").doc("comp_1").update({
      status: "RESOLVED",
      adminNote: "নিজেই সমাধান করলাম",
      updatedAt: pastTime(500),
    })
  );

  // Admin can resolve complaint
  await assertSucceeds(
    adminDb.collection("complaints").doc("comp_1").update({
      status: "RESOLVED",
      adminNote: "কুরিয়ারের সাথে কথা বলে ডেলিভারি নিশ্চিত করা হয়েছে।",
      updatedAt: pastTime(200),
    })
  );
});
