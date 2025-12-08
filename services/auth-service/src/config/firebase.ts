import * as admin from 'firebase-admin';
import * as path from 'path';

// Path to service account JSON file
const serviceAccountPath = path.join(process.cwd(), 'lambrk-messenger-5161a-firebase-adminsdk-y10oc-1bd7615781.json');

// Initialize Firebase Admin if not already initialized
if (!admin.apps.length) {
  try {
    const serviceAccount = require(serviceAccountPath);
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount),
    });
  } catch (error) {
    console.error('Error initializing Firebase Admin:', error);
    throw new Error('Failed to initialize Firebase Admin. Please check service account file.');
  }
}

export const firebaseAdmin = admin;
export default admin;

