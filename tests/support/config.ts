import * as dotenv from 'dotenv';
dotenv.config();

export const config = {
  baseURL: process.env.BASE_URL || 'http://localhost:8080',
  headless: process.env.HEADED !== 'true',
  defaultTimeout: 30000,
  credentials: {
    regularUser: {
      userId: process.env.REGULAR_USER_ID || 'USER0001',
      password: process.env.REGULAR_USER_PWD || 'PASSWORD',
    },
    adminUser: {
      userId: process.env.ADMIN_USER_ID || 'ADMIN001',
      password: process.env.ADMIN_USER_PWD || 'PASSWORD',
    },
  },
};
