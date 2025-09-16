import { NgxUiLoaderConfig } from "ngx-ui-loader";
export class NgxLoaderCongif {
    //  https://tdev.app/ngx-ui-loader/demo/ (reference)
    public static masterLoaderConfig: NgxUiLoaderConfig = {
        bgsColor: "#00ACC1",
        bgsOpacity: 0.5,
        bgsPosition: "bottom-right",
        bgsSize: 60,
        bgsType: "ball-spin-clockwise",
        blur: 4,
        delay: 0,
        fastFadeOut: false,
        fgsColor: "#FE4A55",
        fgsPosition: "center-center",
        fgsSize: 50,
        fgsType: "cube-grid",
        gap: 10,
        hasProgressBar: true,
        logoPosition: "center-center",
        logoSize: 100,
        logoUrl: "",
        masterLoaderId: "master",
        maxTime: -1,
        minTime: 300,
        overlayBorderRadius: "0",
        overlayColor: "rgba(40, 40, 40, 0.8)",
        pbColor: "#FE4A55",
        pbDirection: "ltr",
        pbThickness: 3,
        text: "Loading...",
        textColor: "#FFFFFF",
        textPosition: "center-center",
    }
    public static showForeground() {
        return { showForeground: true }
    }
}