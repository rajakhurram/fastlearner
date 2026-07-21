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
                
                        /* PRINT STYLES - Critical for proper printing */
                        @media print {
                            /* Force backgrounds and colors to print */
                            * {
                                -webkit-print-color-adjust: exact !important;
                                print-color-adjust: exact !important;
                                color-adjust: exact !important;
                            }
                
                            /* Remove margins and adjust page size */
                            @page {
                                margin: 0cm;
                                size: A4;
                            }
                
                            body {
                                margin: 0;
                                padding: 0;
                            }
                
                            /* Ensure header background prints */
                            .header {
                                background: #020B35 !important;
                                background: linear-gradient(120deg, rgb(5, 5, 51) 0%, rgba(0, 0, 83, 1) 50%, rgba(59, 40, 204, 1) 100%) !important;
                                -webkit-print-color-adjust: exact !important;
                                print-color-adjust: exact !important;
                            }
                
                            /* Simplify glassmorphic effects for print */
                            .glass-element {
                                background: rgba(255, 255, 255, 0.15) !important;
                                border: 1px solid rgba(255, 255, 255, 0.3) !important;
                                box-shadow: none !important;
                                backdrop-filter: none !important;
                                -webkit-backdrop-filter: none !important;
                            }
                
                            /* Ensure decorative elements print */
                            .header-info::before,
                            .header-info::after {
                                -webkit-print-color-adjust: exact !important;
                                print-color-adjust: exact !important;
                            }
                
                            .header-info::before {
                                background: #ffffff3f !important;
                                filter: none !important;
                                opacity: 0.3;
                            }
                
                            .header-info::after {
                                background: rgba(254, 74, 86, 0.467) !important;
                                filter: none !important;
                                opacity: 0.5;
                            }
                
                            /* Ensure gradient backgrounds print */
                            .info-box-icon.trophy-box {
                                background: #2B2BFF !important;
                                -webkit-print-color-adjust: exact !important;
                            }
                
                            .info-box-icon.brain-box {
                                background: #FF1624 !important;
                                -webkit-print-color-adjust: exact !important;
                            }
                
                            /* Ensure profile circle prints correctly */
                            .profile {
                                background: rgba(255, 255, 255, 0.2) !important;
                                outline: 2px solid rgba(255, 255, 255, 0.5) !important;
                            }
                
                            /* Prevent page breaks in critical sections */
                            .header,
                            .info-box,
                            .header-left {
                                page-break-inside: avoid;
                                break-inside: avoid;
                            }
                
                            /* Ensure images print */
                            img {
                                max-width: 100%;
                                page-break-inside: avoid;
                                display: block;
                            }
                
                            /* Adjust container padding for print */
                            .container {
                                padding: 15px;
                                max-width: 100%;
                            }
                
                            /* Footer positioning */
                            .footer {
                                page-break-inside: avoid;
                                margin-top: 30px;
                            }
                
                            /* Ensure text colors are visible */
                            .header-title,
                            .glass-element,
                            .info-box span,
                            .info-box small {
                                color: white !important;
                                -webkit-print-color-adjust: exact !important;
                            }
                
                            /* Handle Chart.js canvas for print */
                            canvas {
                                max-width: 100%;
                                height: auto !important;
                            }
                        }
                
                        /* Screen-only adjustments */
                        @media screen {
                            /* Original styles remain for screen view */
                        }
                    </style>
                </head>
                
                <body>
                
                <!-- HEADER -->
                <table width="100%" cellpadding="0" cellspacing="0" border="0"
                       style="background-color: #000053;">
                    <tr>
                        <td style="padding: 25px 30px;">
                            <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                <tr>
                                    <!-- LEFT -->
                                    <td width="55%" valign="middle">
                                        <img src="https://storage.googleapis.com/fastlearner-bucket/icons/logo.svg"
                                             alt="Fast Learner" width="160" height="auto"
                                             style="display: block; margin-bottom: 25px;">
                                        <table cellpadding="0" cellspacing="0" border="0">
                                            <tr>
                                                <td style="background: rgba(255,255,255,0.15);
                                                           border: 1px solid rgba(255,255,255,0.3);
                                                           border-radius: 10px; padding: 8px;
                                                           text-align: center; vertical-align: middle;">
                                                    <img src="https://storage.googleapis.com/fastlearner-bucket/icons/file.svg"
                                                         alt="Report" width="20" height="20" style="display: block;">
                                                </td>
                                                <td style="padding-left: 12px; color: #ffffff;
                                                           font-size: 20px; font-weight: 400;">
                                                    {{TOPIC_TITLE}}
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                    <!-- RIGHT - Info Box -->
                                    <td width="45%" valign="middle" align="right">
                                        <table cellpadding="0" cellspacing="0" border="0" align="right"
                                               style="background: rgba(255,255,255,0.12);
                                                      border: 1px solid rgba(255,255,255,0.25);
                                                      border-radius: 15px; width: 270px;">
                                            <tr>
                                                <td style="padding: 15px;">
                                                    <!-- Student Row -->
                                                    <table cellpadding="0" cellspacing="0" border="0" width="100%">
                                                        <tr>
                                                            <td width="35" valign="middle">
                                                                <div style="width: 35px; height: 35px;
                                                                            background: rgba(255,255,255,0.2);
                                                                            border: 2px solid rgba(255,255,255,0.4);
                                                                            border-radius: 50%; text-align: center;
                                                                            line-height: 35px; font-size: 12px;
                                                                            font-weight: 600; color: #ffffff;">
                                                                    {{STUDENT_INITIALS}}
                                                                </div>
                                                            </td>
                                                            <td valign="middle" style="padding-left: 10px;">
                                                                <div style="color: #ffffff; font-size: 13px; font-weight: 500;">
                                                                    {{STUDENT_NAME}}
                                                                </div>
                                                                <div style="color: rgba(255,255,255,0.7); font-size: 11px;">
                                                                    Student
                                                                </div>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                    <!-- Divider -->
                                                    <div style="border-top: 1px solid rgba(255,255,255,0.2); margin: 10px 0;"></div>
                                                    <!-- Date -->
                                                    <table cellpadding="0" cellspacing="0" border="0" style="margin-bottom: 6px;">
                                                        <tr>
                                                            <td valign="middle">
                                                                <img src="https://storage.googleapis.com/fastlearner-bucket/icons/calendar.svg"
                                                                     width="13" height="13" style="display: block;">
                                                            </td>
                                                            <td style="padding-left: 6px; color: rgba(255,255,255,0.85); font-size: 12px;">
                                                                {{REPORT_DATE}}
                                                            </td>
                                                        </tr>
                                                    </table>
                                                    <!-- Duration -->
                                                    <table cellpadding="0" cellspacing="0" border="0" style="margin-bottom: 6px;">
                                                        <tr>
                                                            <td valign="middle">
                                                                <img src="https://storage.googleapis.com/fastlearner-bucket/icons/clock.svg"
                                                                     width="13" height="13" style="display: block;">
                                                            </td>
                                                            <td style="padding-left: 6px; color: rgba(255,255,255,0.85); font-size: 12px;">
                                                                Duration: {{DURATION}} minutes
                                                            </td>
                                                        </tr>
                                                    </table>
                                                    <!-- Instructor -->
                                                    <table cellpadding="0" cellspacing="0" border="0">
                                                        <tr>
                                                            <td valign="middle">
                                                                <img src="https://storage.googleapis.com/fastlearner-bucket/icons/user.svg"
                                                                     width="13" height="13" style="display: block;">
                                                            </td>
                                                            <td style="padding-left: 6px; color: rgba(255,255,255,0.85); font-size: 12px;">
                                                                Instructor: {{INSTRUCTOR_NAME}}
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                
                <main class="container">
                    <div>{{REPORT_CONTENT}}</div>
                </main>
                
                <footer class="container footer">
                    <p class="text-center">Auto-generated Report | Fast Learner | Page 1</p>
                </footer>
                </body>
                
                </html>
                """;
    }
}