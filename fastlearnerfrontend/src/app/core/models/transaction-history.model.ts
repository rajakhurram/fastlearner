export interface TransactionHistory {
    id:number;
    subscriptionId: Subscription;
    creationAt: Date; 
    authSubscriptionId: string; 
    subscriptionAmount: number | null;
    subscriptionStatus: SubscriptionStatus; 
    responseCode: string | null;
    responseText: string | null;
    customerPaymentProfileId: string | null; 
    updatedDate: Date | null;
    trialEndDate: Date | null; 
    subscriptionNextCycle: Date | null;
    user: User ;
    oldTransactionId: number | null;
    status: GenericStatus;
  }
  
  export enum SubscriptionStatus {
    ACTIVE = 'ACTIVE',
    INACTIVE = 'INACTIVE',
    CANCELLED = 'CANCELLED',
  }
  
  export enum GenericStatus {
    ENABLED = 'ENABLED',
    DISABLED = 'DISABLED',
    PENDING = 'PENDING',
  }
  export interface Subscription {
    id: number; 
    name: string; 
    description: string; 
    price: number; 
    duration: number;
    durationInWord: string;
    paypalPlanId: string;
    isActive: boolean;
  }

  export interface User {
    id: number; 
    fullName: string; 
    email: string;
    stripeAccountId: string | null;
    role: Role;
    provider: AuthProvider; 
    creationDate: Date; 
    loginTimestamp: Date | null; 
    isSubscribed: boolean; 
    salesRaise: number; 
    isActive: boolean;
  }
  
  export enum AuthProvider {
    LOCAL = 'LOCAL',
    GOOGLE = 'GOOGLE',
    FACEBOOK = 'FACEBOOK',
   
  }
  export interface Role {
    id: number;
    type: string; 
  }