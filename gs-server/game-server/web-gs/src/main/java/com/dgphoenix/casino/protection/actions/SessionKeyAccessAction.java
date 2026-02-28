package com.abs.casino.protection.actions;

import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.util.RNG;
import com.abs.casino.common.util.RSACrypter;
import com.dgphoenix.casino.common.util.string.HexStringConverter;
import com.dgphoenix.casino.gs.persistance.PlayerSessionPersister;
import com.abs.casino.protection.forms.SessionKeyAccessForm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class SessionKeyAccessAction extends Action {

    private final static Logger logger = LogManager.getLogger(SessionKeyAccessAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        SessionKeyAccessForm keyAccessForm = (SessionKeyAccessForm) form;
        String encrMess = null;
        if (keyAccessForm.getEncrMess() != null) {
            encrMess = keyAccessForm.getEncrMess();
        } else {
            logger.error("Failed to get 'encrMess' request param");
            return mapping.findForward("success");
        }
        String decrMess = RSACrypter.getDecrMessage(encrMess, getExp2(), getModulus2());

        //logger.debug("SessionKeyAccessAction:: decr mes=" + decrMess);

        StringTokenizer tokenizer = new StringTokenizer(decrMess, "=&");
        Map<String, String> params = new HashMap<String, String>();
        for (int i = 0; i < 2; ++i) {
            params.put(tokenizer.nextToken(), tokenizer.nextToken());
        }

        String sid = params.get("sid");
        SessionHelper.getInstance().lock(sid);
        try {
            SessionHelper.getInstance().openSession();
            SessionInfo sessionInfo = PlayerSessionPersister.getInstance().getSessionInfo();
            String sessionKey = generateDESSessionKey();
            //logger.debug("SessionKeyAccessAction:: key=" + sessionKey);
            sessionInfo.setSecretKey(sessionKey);
            SessionHelper.getInstance().commitTransaction();
            response.getWriter().write("encrMess=" + RSACrypter.getEncrMessage("key=" + sessionKey +
                    "&rnd=" + RNG.nextLong(), getExp1(), getModulus1()));
            SessionHelper.getInstance().markTransactionCompleted();
        } finally {
            SessionHelper.getInstance().clearWithUnlock();
        }
        response.getWriter().flush();
        return mapping.findForward("success");
    }

    private String generateDESSessionKey() {
        try {
            SecretKey key = KeyGenerator.getInstance("DES").generateKey();
            //logger.debug("SessionKeyAccessAction:: key generate ok");
            return HexStringConverter.byteArrayToHexString(key.getEncoded());
        } catch (Exception e) {
            logger.error("key generate failed", e);
            return null;
        }

    }

    private BigInteger getExp1() {
        return new BigInteger("6926263069740087303969943302004575455852273530530025217762477133927279721785304663041638587877914730619717958945826808730045764577679965023005026696022273");
    }

    private BigInteger getExp2() {
        return new BigInteger("1489307138066038745264009663797714972900700663787428807197565358077440022818898213219527715675256947557984741531805588100209781275883631094929504809279713");
    }

    private BigInteger getModulus1() {
        return new BigInteger("9870113128974909798657929423428437957168742125904463202794074014485586673769355574291696945170634391535083008867787612551904374904294465957904956791989653");
    }

    private BigInteger getModulus2() {
        return new BigInteger("8745942823246772513294570012214233528583621810272107682554376422251002399236937256638852921937597413101887877198244614153477316299631506835065041224442353");
    }
}
