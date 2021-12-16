package gtrboy.learning.IKEv2.parsers;

import gtrboy.learning.utils.LogUtils;

import java.net.DatagramPacket;

public class IKEv2SaInitParser extends IKEv2Parser {

    private byte[] key = null;
    private byte[] nonce = null;

    public IKEv2SaInitParser(DatagramPacket pkt){
        super(pkt);
    }

    @Override
    public String parsePacket() {
        String retstr = null;
        boolean isSA = false;
        boolean isKE = false;
        boolean isNC = false;
        while(nPld != 0) {
            switch (nPld) {
                case 0x29:  // Notify
                    parseNotifyPayload();
                    break;
                case 0x21:  // SA
                    parseSAPayload();
                    isSA = true;
                    break;
                case 0x22:  // KE
                    parseKEPayload();
                    isKE = true;
                    break;
                case 0x28:  // Nonce
                    parseNoncePayload();
                    isNC = true;
                    break;
                default:
                    parseDefault();
            }
        }
        // Types in range 0~16383 are inteded for reporting errors.
       if(notifyType <= NOTIFY_ERROR_MAX && notifyType != 0){
            retstr = NOTIFY_TYPES.get(Integer.valueOf(notifyType));
            if (retstr == null){
                LogUtils.logErrExit(this.getClass().getName(), "Unknown Notify Type! ");
            }
        } else if(isSA && isKE && isNC){
           //retstr = "RESP_IKE_INIT_SA";
           retstr = "OK";
       } else{
            LogUtils.logErrExit(this.getClass().getName(), "Receive wrong IKE_INIT_SA! ");
        }

        return retstr;
    }

    /* Fix the Security Association */
    private void parseSAPayload(){
        int pLen = parsePayloadHdr();
        AO(pLen-4);
    }

    private void parseKEPayload(){
        int keyLen = parsePayloadHdr() - 8;
        AO(4);
        key = new byte[keyLen];
        System.arraycopy(pb, AO(keyLen), key, 0, keyLen);
    }

    public byte[] getPubKey(){
        if (key != null) {
            return key;
        }else{
            LogUtils.logErrExit(this.getClass().getName(), "Get peer's public key error!");
        }
        return null;
    }

    private void parseNoncePayload(){
        int nonceLen = parsePayloadHdr() - 4;
        nonce = new byte[nonceLen];
        System.arraycopy(pb, AO(nonceLen), nonce, 0, nonceLen);
    }

    public byte[] getNonce(){
        if (nonce != null) {
            return nonce;
        }else{
            LogUtils.logErrExit(this.getClass().getName(), "Get peer's nonce error!");
        }
        return null;
    }


    /*
    private void parseSAPayload(){
        int pLen = parsePayloadHdr() - 4;
        parseProposal();

    }

    private void parseProposal(){
        AO(7);
        int transNum = pb[AO(1)] & 0xFF;
        for (int i=0; i<transNum; i++){
            parseTransform();
        }
    }

    private void parseTransform(){
        AO(2);
        short remainedLen = (short) (DataUtils.bytesToShortB(pb, AO(2)) - 4);
        byte transType = pb[AO(1)]; remainedLen -= 1;
        AO(1); remainedLen -= 1;
        byte transID = pb[AO(1)]; remainedLen -= 1;
        if (remainedLen > 0){

        }

    }

     */
}
