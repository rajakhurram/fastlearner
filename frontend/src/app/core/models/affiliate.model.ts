export interface InstructorAffiliate {
  instructorAffiliateId: number;
  affiliateId: number;
  name: string;
  nickName: string;
  email: string;
  rewards: number;
  totalRevenue?: number;
  onboardStatus?: any;
  totalOnboardedStudent?: number;
  isSelf?:Boolean;
}
export interface InstructorPremiumCourses {
  courseDescription: string;
  courseId: number;
  courseTitle: string;
  thumbnailUrl: string;
  panelOpen?: boolean;
  courseUrl?: string;
}
export interface AssignedPremiumCourses {
  affiliateId?: any;
  affiliateName?: any;
  assignDate: string;
  courseId: number;
  courseTitle: string;
  id: number;
  instructorAffiliateId: number;
  revenue?: any;
  reward: number;
  students: number;
  url: string;
  status:any;
  switchValue:boolean;
}
