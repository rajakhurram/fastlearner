import { PaymentProfile } from './payment-profile.model';

export class Subscription {
  subscriptionId?: any;
  coupon?: string;
  amount?: number;
  paymentDetail?: PaymentProfile;
}
export class CheckoutData {
  opaqueData: string;
  courseId: number;
  affiliateUUID?: any;
  coupon?: string;
}
