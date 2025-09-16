export enum CourseStatus {
  PUBLISHED = 'PUBLISHED',
  UNPUBLISHED = 'UNPUBLISHED',
  DRAFT = 'DRAFT',
  DELETE = 'DELETE',
}

export enum CourseType {
  PREMIUM = 'PREMIUM_COURSE',
  STANDARD = 'STANDARD_COURSE',
  FREE = 'FREE_COURSE',
}
export enum ViewAllMap {
  PREMIUM = 'PREMIUM_COURSE',
  TRENDING = 'TRENDING_COURSE',
  FREE = 'FREE_COURSE',
  NEW = 'NEW_COURSE',
  CATEGORY = 'CATEGORY_COURSE',
}

export const courseTypeArray = [
  {
    name: 'All',
    value: 'ALL_COURSE',
    disabled: false
  },
  {
    name: 'Free',
    value: 'FREE_COURSE',
    disabled: false
  },
  {
    name: 'Standard',
    value: 'STANDARD_COURSE',
    disabled: false
  },
  {
    name: 'Premium',
    value: 'PREMIUM_COURSE',
    disabled: false
  }
];

export const contentTypeArray = [
  {
    name: 'Course',
    value: 'COURSE',
  },
  {
    name: 'Test',
    value: 'TEST',
  }
]

export enum CourseTypeMap {
  TRENDING = 'TRENDING_COURSE',
  PREMIUM = 'PREMIUM_COURSE',
  FREE = 'FREE_COURSE',
  NEW = 'NEW_COURSE',
  CATEGORY = 'CATEGORY_COURSE',
}

export function mapCourseType(type: string): string | undefined {
  const mapping: { [key: string]: CourseTypeMap } = {
    trending: CourseTypeMap.TRENDING,
    premium: CourseTypeMap.PREMIUM,
    free: CourseTypeMap.FREE,
    new: CourseTypeMap.NEW,
    category: CourseTypeMap.CATEGORY,
  };

  return type ? mapping[type.toLowerCase()] : null;
}

export const courseTypeMapper: { [key: string]: string } = {
  TRENDING_COURSE: 'Trending',
  PREMIUM_COURSE: 'Premium',
  FREE_COURSE: 'Free',
  NEW_COURSE: 'New',
  CATEGORY_COURSE: 'Category',
};

export const filterFeaturedArray = [
  {
    name: 'Trending',
    value: 'TRENDING_COURSE',
  },
  {
    name: 'New',
    value: 'NEW_COURSE',
  }
];
