import { CourseStatus } from '../enums/course-status';

export function isPremiumCourseType(courseType: unknown): boolean {
  const normalized = (courseType ?? '').toString().toUpperCase();
  return normalized === 'PREMIUM' || normalized === 'PREMIUM_COURSE';
}

/** Published/unpublished premium courses are read-only; drafts remain editable. */
export function isPremiumCourseEditBlocked(course?: {
  courseType?: unknown;
  courseStatus?: unknown;
}): boolean {
  if (!course || !isPremiumCourseType(course.courseType)) {
    return false;
  }

  const status = (course.courseStatus ?? '').toString().toUpperCase();
  if (!status) {
    return false;
  }
  return status !== CourseStatus.DRAFT;
}
