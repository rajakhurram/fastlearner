package com.vinncorp.fast_learner.services.reports;

public class ReportString {
    public static String template() {
        return """
                <!DOCTYPE html>
                <html lang="en">
                
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Personality Report</title>
                    <link rel="preconnect" href="https://fonts.googleapis.com">
                    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                    <link href="https://fonts.googleapis.com/css2?family=Poppins:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,100;1,200;1,300;1,400;1,500;1,600;1,700;1,800;1,900&display=swap" rel="stylesheet">
                    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
                    <style>
                        * {
                            padding: 0;
                            margin: 0;
                            box-sizing: border-box;
                            font-family: 'Poppins', system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
                            font-size: 14px;
                        }
                
                        body {
                            margin: 0 !important;
                        }
                
                        .container {
                            max-width: 1200px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                
                        .header {
                            background: #020B35;
                            background: linear-gradient(120deg, rgb(5, 5, 51) 0%, rgba(0, 0, 83, 1) 50%, rgba(59, 40, 204, 1) 100%);
                        }
                
                        .flex {
                            display: flex;
                        }
                
                        .flex-column {
                            flex-direction: column;
                        }
                
                        .align-center {
                            align-items: center;
                        }
                
                        .justify-between {
                            justify-content: space-between;
                        }
                
                        .gap-5 {
                            gap: 5px;
                        }
                
                        .gap-10 {
                            gap: 10px;
                        }
                
                        .gap-15 {
                            gap: 15px;
                        }
                
                        .gap-20 {
                            gap: 20px;
                        }
                
                        .gap-25 {
                            gap: 25px;
                        }
                
                        .font-small {
                            font-size: 12px;
                        }
                
                        .font-light {
                            font-weight: 300;
                        }
                
                        .space-y-5>*+* {
                            margin-top: 5px;
                        }
                
                        .space-y-10>*+* {
                            margin-top: 10px;
                        }
                
                        .text-center {
                            text-align: center;
                        }
                
                        .glass-element {
                            background: rgba(255, 255, 255, 0.1);
                            backdrop-filter: blur(10px);
                            -webkit-backdrop-filter: blur(10px);
                            border-radius: 15px;
                            border: 1px solid rgba(255, 255, 255, 0.2);
                            box-shadow: -6px -8px 32px rgba(0, 0, 0, 0.25), inset 1px 1px 1px 0px rgb(255 255 255 / 64%);
                            color: white;
                        }
                
                        .header-left {
                            flex: 1;
                            display: flex;
                            flex-direction: column;
                            gap: 50px;
                        }
                
                        .header-report-icon {
                            width: 50px;
                            height: 50px;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                        }
                
                        .header-info {
                            position: relative;
                            max-width: 285px;
                            width: 100%;
                        }
                
                        .header-info::before {
                            content: "";
                            position: absolute;
                            right: -10px;
                            top: -10px;
                            width: 50px;
                            height: 50px;
                            background: #ffffff3f;
                            filter: blur(10px);
                            border-radius: 15px;
                            box-shadow: 7px -5px 12px 0px rgb(255 255 255 / 25%);
                            transform: rotate(26deg);
                        }
                
                        .header-info::after {
                            content: "";
                            position: absolute;
                            left: -27px;
                            bottom: -9px;
                            width: 50px;
                            height: 50px;
                            background: rgba(254, 74, 86, 0.467);
                            filter: blur(10px);
                            border-radius: 15px;
                            box-shadow: 7px -5px 12px 0px rgba(254, 74, 86, 0.467);
                            transform: rotate(65deg);
                        }
                
                        .header-title {
                            font-size: 22px;
                            font-weight: 400;
                            color: white;
                            margin-left: 15px;
                        }
                
                        .info-box {
                            max-width: 300px;
                            width: 100%;
                            margin-left: auto;
                            padding: 15px;
                            position: relative;
                            z-index: 1;
                        }
                
                        .profile {
                            width: 35px;
                            height: 35px;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            border-radius: 50%;
                            font-size: 12px;
                            font-weight: 500;
                            background: rgba(255, 255, 255, 0.1);
                            backdrop-filter: blur(10px);
                            -webkit-backdrop-filter: blur(10px);
                            outline: 2px solid rgba(255, 255, 255, 0.308);
                        }
                
                        .info-box-icon {
                            position: absolute;
                            width: 40px;
                            height: 40px;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            border-radius: 12px;
                        }
                
                        .info-box-icon.trophy-box {
                            top: -10px;
                            right: -15px;
                            background: linear-gradient(131deg, #3030b6, #2B2BFF);
                            z-index: 10;
                            transform: rotate(15deg);
                        }
                
                        .info-box-icon.brain-box {
                            bottom: -10px;
                            left: -27px;
                            background: linear-gradient(131deg, #FE4A55, #FF1624);
                            z-index: 10;
                            transform: rotate(346deg);
                        }
                
                        .footer {
                            color: #6A7282;
                            border-top: 1px solid #E5E7EB;
                            margin-top: 40px;
                            padding-top: 20px;
                        }
                    </style>
                </head>
                
                <body>
                <header class="header">
                    <div class="container flex align-center justify-between">
                        <div class="header-left">
                            <img src="https://storage.googleapis.com/fastlearner-bucket/icons/logo.svg" alt="Company Logo" class="logo" width="200" height="auto">
                            <div class="flex align-center">
                                <div class="glass-element header-report-icon">
                                    <img src="https://storage.googleapis.com/fastlearner-bucket/icons/file.svg" alt="Report" width="25" height="25">
                                </div>
                                <p class="header-title">{{TOPIC_TITLE}}</p>
                            </div>
                        </div>
                        <div class="header-info">
                            <div class="info-box-icon trophy-box">
                                <img src="https://storage.googleapis.com/fastlearner-bucket/icons/trophy.svg" alt="Trophy" width="20" height="auto">
                            </div>
                            <div class="glass-element info-box space-y-10">
                                <div class="flex align-center gap-10">
                                    <div class="profile">{{STUDENT_INITIALS}}</div>
                                    <div class="flex flex-column">
                                        <span>{{STUDENT_NAME}}</span>
                                        <small class="font-small font-light">Student</small>
                                    </div>
                                </div>
                                <div class="font-small font-light space-y-5">
                                    <p class="flex align-center gap-5">
                                        <img src="https://storage.googleapis.com/fastlearner-bucket/icons/calendar.svg" alt="calendar" width="16" height="auto">
                                        <span>{{REPORT_DATE}}</span>
                                    </p>
                                    <p class="flex align-center gap-5">
                                        <img src="https://storage.googleapis.com/fastlearner-bucket/icons/clock.svg" alt="clock" width="16" height="auto">
                                        <span>Duration: <span>{{DURATION}}</span> minutes</span>
                                    </p>
                                    <p class="flex align-center gap-5">
                                        <img src="https://storage.googleapis.com/fastlearner-bucket/icons/user.svg" alt="instructor" width="16" height="auto">
                                        <span>Instructor: <span>{{INSTRUCTOR_NAME}}</span></span>
                                    </p>
                                </div>
                            </div>
                            <div class="info-box-icon brain-box">
                                <img src="https://storage.googleapis.com/fastlearner-bucket/icons/brain.svg" alt="Brain" width="20" height="auto">
                            </div>
                        </div>
                    </div>
                </header>
                
                <main class="container">
                    <div>{{REPORT_CONTENT}}</div>
                </main>
                
                <footer class="container footer">
                    <p class="text-center">Auto-generated Report | Fast Learner | Page 1</p>
                </footer>
                </body>
                
                </html>""";
    }
}
