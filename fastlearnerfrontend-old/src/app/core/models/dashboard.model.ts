export interface Dashboard {
    completionRate: number,
    totalParticipants: number,
    revenue: number,
    totalStudents: DashboardValue,
    totalProfileVisits: DashboardValue,
}

export interface DashboardValue {
    totalValue: number,
    values: Value
}

export interface Value {
    monthDate: any,
    value: number
}