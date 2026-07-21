export class Coupon {
  constructor(
    public id: string | number | null = null,
    public coupon: string = '',
    public discountUnit: string = '',
    public discount: number = 0,
    public subscriptionId: string | number = '',
    public startDate: any = '',
    public endDate: any = '',
    public appliesTo: string = '',
    public specifiedCourses: string[] = [],
    public specifiedUsers: string[] = [],
    public isActive: boolean = true,
    public specifiedDomains: string[] = [],
    public couponType: string = '',
    public billingCycle: string = '',
    public durationInMonth: number = 0,
  ) {}
}
