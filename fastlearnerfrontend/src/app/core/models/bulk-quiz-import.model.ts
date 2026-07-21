export interface BulkQuizImportModalResult {
  questions: any[];
  fileName: string | null;
  successCount?: number;
  errorCount?: number;
  imageUrlMap?: Record<string, string>;
}
