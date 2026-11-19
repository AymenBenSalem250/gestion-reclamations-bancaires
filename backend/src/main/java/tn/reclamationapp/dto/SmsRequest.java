package tn.reclamationapp.dto;

public class SmsRequest {

    private String textMsg;
    private String destNum;
    private String codOpe;
    private String codMod;

    public SmsRequest() {
    }

    public SmsRequest(String textMsg, String destNum, String codOpe, String codMod) {
        this.textMsg = textMsg;
        this.destNum = destNum;
        this.codOpe = codOpe;
        this.codMod = codMod;
    }

    public String getTextMsg() {
        return textMsg;
    }

    public void setTextMsg(String textMsg) {
        this.textMsg = textMsg;
    }

    public String getDestNum() {
        return destNum;
    }

    public void setDestNum(String destNum) {
        this.destNum = destNum;
    }

    public String getCodOpe() {
        return codOpe;
    }

    public void setCodOpe(String codOpe) {
        this.codOpe = codOpe;
    }

    public String getCodMod() {
        return codMod;
    }

    public void setCodMod(String codMod) {
        this.codMod = codMod;
    }
}
