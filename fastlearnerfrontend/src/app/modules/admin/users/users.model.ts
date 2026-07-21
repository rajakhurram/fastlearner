export class Users {
  constructor(
    public search: string,
    public planType: string,
    public subscriptionStatus: string,
    public dateFrom: string,
    public dateTo: string,
    public page: number,
    public size: number,
  ) {}
}
