export class Regex {
    NAME : string = '^([a-zA-Z]+\s)*[a-zA-Z]+$'
    EMAIL : string = '^([a-zA-Z0-9]+([\._-]?[a-zA-Z0-9]+)*@\w+([\.-]?\w+)*(\.\w{2,3})+)*$'
    PHONE_NUMBER : string = '^((\+92)|(0092))-{0,1}\d{3}-{0,1}\d{7}$|^\d{11}$|^\d{4}-\d{7}$'
}