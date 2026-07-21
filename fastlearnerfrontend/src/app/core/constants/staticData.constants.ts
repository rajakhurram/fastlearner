import { environment } from 'src/environments/environment.development';

export const isProductionServer = environment.isProductionServer;

export const cards = [
  {
    imageUrl: '../../../../assets/images/static1-webp.webp',
    heading: 'Explore',
    content:
      'Discover the latest courses powered by exclusive AI features on our AI-based learning platform, unveiling a world of quick learning possibilities tailored to your interests.',
  },
  {
    imageUrl: '../../../../assets/images/static2-webp.webp',
    heading: 'Choose',
    content:
      'Handpick the course that resonates with your dream, ensuring your AI learning platform bridges you to your goals and helps you understand how to become a fast learner.',
  },
  {
    imageUrl: '../../../../assets/images/static3-webp.webp',
    heading: 'Subscribe',
    content:
      'Subscribe to our monthly or annual plan to instantly access all courses, including those created with AI powered course creation, plus exclusive resources and expert answers.',
  },
  {
    imageUrl: '../../../../assets/images/static4-webp.webp',
    heading: 'Grow',
    content:
      'Witness your expertise flourish with fast learner techniques, quick to learn methods, and interactive lessons while collaborating with a vibrant community.',
  },
  {
    imageUrl: '../../../../assets/images/static5-webp.webp',
    heading: 'Sign In',
    content:
      'Start your learning journey today and become part of our platform',
    showButton: true,
  },
];
export const instructorCards = [
  {
    imageUrl: '../../../../assets/images/inst4.png',
    heading: 'Hamza Farooq',
    url: `${environment.production ? (environment.isProductionServer ? 'https://fastlearner.ai' : 'https://staging.fastlearner.ai') : 'http://localhost:4200'}/user/profile?url=hamza-farooq`,
    content: 'Machine Learning',
    flipContent:
      'I am an Instructor at Stanford Continuing Studies and an Adjunct Faculty Member at UCLA. By day, I am a founder, and by night, I teach and conduct research focused on NLP and Multi-Modal Systems. I founded traversaal.ai to build scalable, customizable, and cost-efficient LLM solutions for enterprises. Before starting my entrepreneurial journey, I worked as a Research Manager at Google, where I contributed to advancing AI and machine learning technologies.',
    flipImage: '../../../../assets/images/instFlipImage1.png',
  },
  {
    imageUrl: '../../../../assets/images/inst5.png',
    heading: 'Joseph Labrecque',
    url: `${environment.production ? (environment.isProductionServer ? 'https://fastlearner.ai' : 'https://staging.fastlearner.ai') : 'http://localhost:4200'}/user/profile?url=joseph-labrecque`,
    content: 'Creative Design',
    flipContent:
      'I am a creative developer, designer, and educator with nearly two decades of experience building innovative web, desktop, and mobile solutions. I joined the University of Colorado Boulder College of Media, Communication and Information in 2019 as a faculty member in the Department of Advertising, Public Relations and Media Design. My teaching focuses on creative software, digital workflows, user interaction, and design principles. Prior to joining CU Boulder, I served as adjunct faculty at the University of Denver and worked as a senior interactive software engineer, user interface developer, and digital media designer.',
    flipImage: '../../../../assets/images/instFlipImage5.png',
  },
  {
    imageUrl: '../../../../assets/images/inst3.png',
    heading: 'Shewta Mogha',
    url: `${environment.production ? (environment.isProductionServer ? 'https://fastlearner.ai' : 'https://staging.fastlearner.ai') : 'http://localhost:4200'}/user/profile?url=shweta-mogha`,
    content: 'SPHR, ACC, CPCC',
    flipContent:
      'I am a Global Human Resources and Talent Acquisition leader, DEI expert, and ICF-certified executive coach with extensive experience leading people strategy at Meta, Amazon, and Airtel. Throughout my career, I have spearheaded high-impact diversity and inclusion initiatives, including Return to Work for Women, Empowered Women of the World, and Veterans Hiring programs. I have also led global change management initiatives focused on leadership development, competency frameworks, career pathing, 360-degree assessments, and employee engagement. My leadership development courses have been published on LinkedIn Learning, Skillsoft Percipio, and other learning platforms. I have worked across North America, Europe, India, Sri Lanka, Bangladesh, and Africa, supporting diverse teams and driving impactful people strategies.',
    flipImage: '../../../../assets/images/instFlipImage4.png',
  },
  {
    imageUrl: '../../../../assets/images/inst2.png',
    heading: 'Khurram Kalimi',
    url: `${environment.production ? (environment.isProductionServer ? 'https://fastlearner.ai' : 'https://staging.fastlearner.ai') : 'http://localhost:4200'}/user/profile?url=khurram-kalimi-40`,
    content: 'Sales',
    flipContent:
      'I am a computer science graduate and masters in business administration. After 4 years of software development experience I worked at different sales roles for 10 years in companies like Oracle, Microsoft and VMWare. After 14 years of my career I realised that Entrepreneurship is my forte and I ventured as Co-Founder at VinnCorp. Here we are trying to make people life easy.',
    flipImage: '../../../../assets/images/instFlipImage3.png',
  },
  {
    imageUrl: '../../../../assets/images/inst1.png',
    heading: 'Rashim Mogha',
    url: `${environment.production ? (environment.isProductionServer ? 'https://fastlearner.ai' : 'https://staging.fastlearner.ai') : 'http://localhost:4200'}/user/profile?url=rashim-mogha`,
    content: 'Career Development',
    flipContent:
      "I'm Rashim Mogha—Amazon #1 best-selling author, keynote speaker, and  Forbes Business Council member. My LinkedIn Learning courses have empowered over 250,000 professionals globally. I am a Gen X er and on this channel, you’ll find educational content on personal development, productivity, and career growth",
    flipImage: '../../../../assets/images/instFlipImage2.png',
  },
];

export const assignpremium = [
  {
    id: '45',
    title: 'Artificial Intelligence and expert systems',
    outcome:
      'Understanding the core concepts and applications of artificial intelligence.\nMastering the syntax and programming constructs used in AI development.\nOrganizing and structuring AI projects efficiently.',
    description:
      '<p>This comprehensive course on Artificial Intelligence covers everything from basic concepts to advanced techniques. Learners will start with an introduction to AI, understand its syntax, and learn how to structure their AI projects effectively. The course includes hands-on exercises and real-world examples to ensure practical understanding. By the end of the course, students will be equipped with the knowledge and skills to develop and implement AI solutions in various domains.</p>',
    tags: [
      'ArtificialIntelligence ',
      'AI ',
      'MachineLearning ',
      'DeepLearning',
      'python',
    ],
    thumbnailUrl:
      'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/hYo5hNon_profile_image.jpeg',
    creatorName: 'David Bomb',
    userProfileUrl: 'david-bomb',
    profilePictureUrl:
      'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/fqQq6KHJ_profile_image.jpeg',
    courseUrl: 'artificial-intelligence-and-expert-systems',
    rating: 3.3333333333333335,
    numberOfViewers: 3,
    duration: 42559,
    courseType: 'FREE',
    price: null,
    isEnrolled: false,
    panelOpen: false,
    sections: [
      {
        id: '45',
        sectionId: 124,
        name: 'Artificial Intelligence with Python',
        score: 4.891688823699951,
      },
    ],
    topics: [
      {
        id: '45',
        topicId: 244,
        sectionId: 123,
        name: 'Artificial Intelligence Project Ideas _ Artificial Intelligence Training _ Edureka',
        score: 4.288497447967529,
      },
      {
        id: '45',
        topicId: 245,
        sectionId: 123,
        name: 'Artificial Intelligence Tutorial for Beginners _ Artificial Intelligence Explained _ Edureka',
        score: 4.103331565856934,
      },
      {
        id: '45',
        topicId: 275,
        sectionId: 130,
        name: 'Types Of Artificial Intelligence',
        score: 3.9585626125335693,
      },
      {
        id: '45',
        topicId: 278,
        sectionId: 130,
        name: 'What is Artificial Intelligence',
        score: 3.9585626125335693,
      },
    ],
  },
  {
    id: '45',
    title: 'Artificial Intelligence and expert systems',
    outcome:
      'Understanding the core concepts and applications of artificial intelligence.\nMastering the syntax and programming constructs used in AI development.\nOrganizing and structuring AI projects efficiently.',
    description:
      '<p>This comprehensive course on Artificial Intelligence covers everything from basic concepts to advanced techniques. Learners will start with an introduction to AI, understand its syntax, and learn how to structure their AI projects effectively. The course includes hands-on exercises and real-world examples to ensure practical understanding. By the end of the course, students will be equipped with the knowledge and skills to develop and implement AI solutions in various domains.</p>',
    tags: [
      'ArtificialIntelligence ',
      'AI ',
      'MachineLearning ',
      'DeepLearning',
      'python',
    ],
    thumbnailUrl:
      'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/hYo5hNon_profile_image.jpeg',
    creatorName: 'David Bomb',
    userProfileUrl: 'david-bomb',
    profilePictureUrl:
      'https://storage.googleapis.com/fastlearner-bucket/PROFILE_IMAGE/fqQq6KHJ_profile_image.jpeg',
    courseUrl: 'artificial-intelligence-and-expert-systems',
    rating: 3.3333333333333335,
    numberOfViewers: 3,
    duration: 42559,
    courseType: 'FREE',
    price: null,
    isEnrolled: false,
    panelOpen: true,
    sections: [
      {
        id: '45',
        sectionId: 124,
        name: 'Artificial Intelligence with Python',
        score: 4.891688823699951,
      },
    ],
    topics: [
      {
        id: '45',
        topicId: 244,
        sectionId: 123,
        name: 'Artificial Intelligence Project Ideas _ Artificial Intelligence Training _ Edureka',
        score: 4.288497447967529,
      },
      {
        id: '45',
        topicId: 245,
        sectionId: 123,
        name: 'Artificial Intelligence Tutorial for Beginners _ Artificial Intelligence Explained _ Edureka',
        score: 4.103331565856934,
      },
      {
        id: '45',
        topicId: 275,
        sectionId: 130,
        name: 'Types Of Artificial Intelligence',
        score: 3.9585626125335693,
      },
      {
        id: '45',
        topicId: 278,
        sectionId: 130,
        name: 'What is Artificial Intelligence',
        score: 3.9585626125335693,
      },
    ],
  },
];

export const aiGraderCards = [
  {
    imageUrl: '../../../../assets/images/static-1.webp',
    heading: 'Supports Handwriting & Scanned Copies',
    content:
      'FastLearner’s AI grader recognizes typed and handwritten work, ensuring all students are included.',
  },
  {
    imageUrl: '../../../../assets/images/static-2-new.webp',
    heading: 'One-Click Grading for the Whole Class',
    content:
      'Grade dozens of student papers instantly with bulk AI grading, saving hours of manual effort for instructors.',
  },
  {
    imageUrl: '../../../../assets/images/static-3-new.webp',
    heading: 'Easily Export Grading Results to a Spreadsheet',
    content:
      'Export grades, feedback, and performance data to Excel or CSV files easily. Our AI grading tool is perfect for tracking academic performance.',
  },
  {
    imageUrl: '../../../../assets/images/static-4-new.webp',
    heading: 'Send Results Directly to Students Via Email',
    content:
      'FastLearner’s AI grading software automatically emails grades and feedback to students, streamlining communication and reporting.',
  },
  {
    imageUrl: '../../../../assets/images/static-5.webp',
    heading: 'Provides Audit-Ready Grading Reports',
    content:
      'Get detailed, audit-friendly AI grading reports that ensure transparency and compliance with institutional standards.',
  },
];
