package gtrboy.learning.IKEv2.parsers;

import gtrboy.learning.IKEv2.IKEv2KeysGener;
import gtrboy.learning.utils.LogUtils;

import java.net.DatagramPacket;

public class IKEv2RekeyIkeSaParser extends IKEv2EncParser {

    IKEv2KeysGener curKeyG;
    byte[] r_nonce = null;
    byte[] r_ke = null;
    byte[] r_spi = null;


    public IKEv2RekeyIkeSaParser(DatagramPacket pkt, IKEv2KeysGener curKG){
        super(pkt, curKG);
        curKeyG = curKG;
    }

    @Override
    public String parsePacket() {
        String retStr = null;
        boolean isSa = false;
        boolean isNc = false;
        boolean isKe = false;

        while(nPld != 0){
            switch (nPld) {
                case 0x2e:
                    try{
                        parseEncPayload();
                    } catch (Exception e){
                        LogUtils.logException(e, this.getClass().getName(), "Failed to decrypt the enc data!");
                    }
                    break;
                case 0x29:
                    parseNotifyPayload();
                    break;
                case 0x21:   // SA
                    parseSaPayload();
                    isSa = true;
                    break;
                case 0x28:
                    parseNoncePayload();
                    isNc = true;
                    break;
                case 0x22:
                    parseKePayload();
                    isKe = true;
                    break;
                default:
                    parseDefault();

            }
        }

        boolean isNormal = isSa && isNc && isKe;
        if(notifyType <= NOTIFY_ERROR_MAX && notifyType != 0){  // Normal
            retStr = NOTIFY_TYPES.get(Integer.valueOf((int)notifyType));
            //LogUtils.logDebug(this.getClass().getName(), "Notify Type: " + notifyType);
            if(retStr == null){
                LogUtils.logErrExit(this.getClass().getName(), "Unknown Notify Type! ");
            }
        } else if (isNormal){      // Error Notify
            //retStr = "RESP_REKEY_IKE_SA";
            retStr = "OK";
        }else {
            LogUtils.logErrExit(this.getClass().getName(), "Receive wrong REKEY_IKE_SA! ");
        }
        return retStr;
    }

    private void parseNoncePayload(){
        int nonceLen = parsePayloadHdr() - 4;
        r_nonce = new byte[nonceLen];
        System.arraycopy(pb, AO(nonceLen), r_nonce, 0, nonceLen);
    }

    private void parseKePayload(){
        int keyLen = parsePayloadHdr() - 8;
        AO(4);
        r_ke = new byte[keyLen];
        System.arraycopy(pb, AO(keyLen), r_ke, 0, keyLen);
    }

    private void parseSaPayload(){
        int pLen = parsePayloadHdr();
        r_spi = new byte[8];
        AO(8);
        System.arraycopy(pb, AO(8), r_spi, 0, 8);
        AO(pLen - 20);
    }

    public byte[] getNonce(){
        return r_nonce;
    }

    public byte[] getKe(){
        return r_ke;
    }

    public byte[] getRSpi(){
        return r_spi;
    }
}


