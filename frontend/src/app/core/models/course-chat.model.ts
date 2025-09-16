export interface CourseChat {
  sectionId?: number;
  sectionName?: string;
  sequenceNumber?: number;
  topics?: Topic[];
}

export interface Topic {
  topicId?: number;
  topicName?: string;
  chatTopicHistory?: Chat[];
  videoId: number;
  active: boolean;
  disabled: boolean;
  sequence?: number;
  customStyle: {
    background: '#f7f7f7';
    'border-radius': '4px';
    'margin-bottom': '24px';
    border: '0px';
  };
}

export interface Chat {
  chatId?: number;
  time?: string;
  title?: string;
}
