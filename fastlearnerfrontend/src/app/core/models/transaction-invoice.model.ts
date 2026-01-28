export class TransactionInvoice {
    paymentId?: string;
    planDetail?: PlanDetail;
    paymentMethod?: PaymentMethod;
    customer?: Customer;
}

export class PlanDetail {
    name?: string;
    price?: string;
    startDate?: string;
    endDate?: string;
    status?: string;
}

export class PaymentMethod {
    method?: string;
    cardType?: string;
    cardNo?: string;
    expiryDate?: string;
    firstName?: string;
    lastName?: string
}

export class Customer {
    id?: any;
    email?: string;
    name?: string;
}