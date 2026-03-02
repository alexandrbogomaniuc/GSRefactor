<%@ page import="com.abs.casino.common.util.string.StringUtils" %>
<%@ page import="com.abs.casino.common.util.CollectionUtils" %>
<%@ page import="com.abs.casino.common.cache.SubCasinoCache" %>
<%@ page import="com.abs.casino.common.cache.data.bank.SubCasino" %>
<%@ page import="java.util.*" %>
<%@ page import="com.abs.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.abs.casino.common.cache.BankInfoCache" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="com.google.common.base.Joiner" %>

<%

    writer = response.getWriter();

    Map<Long, ArrayList<Long>> targetBanksBySystem = new HashMap();
    Long targetBanksNumber = 0L;
    ArrayList<String> deliveredNotes = new ArrayList();

    List<Long> subCasinoIds;
    if (!StringUtils.isTrimmedEmpty(request.getParameter("subCasinoIds"))) {
        subCasinoIds = CollectionUtils.stringToListOfLongs(request.getParameter("subCasinoIds"), "|");
    } else {
        LOG("Parameter 'subCasinoIds' can't be empty");
        return;
    }

    ArrayList<Long> targetSystems = new ArrayList<>();

    for (Long subCasinoId : subCasinoIds) {
        SubCasino subCasino = SubCasinoCache.getInstance().get(subCasinoId);
        if (subCasino == null) continue;
        ArrayList<Long> targetBanks = findTargetBanks(subCasino);
        if (targetBanks.isEmpty()) continue;

        targetSystems.add(subCasinoId);
        targetBanksBySystem.put(subCasinoId, targetBanks);
        targetBanksNumber += targetBanks.size();
    }

    LOG("<pre>");

    for (Long subCasinoId : targetSystems) {
        SubCasino subCasino = SubCasinoCache.getInstance().get(subCasinoId);

        for (Long bankId : targetBanksBySystem.get(subCasinoId)) {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            String targetForNote = "[" + subCasino.getId()
                    + "-" + subCasino.getName() + ": "
                    + bankInfo.getId() + "-" + bankInfo.getExternalBankId()
                    + bankInfo.getExternalBankIdDescription() + "]: ";

            BankInfoCache.getInstance().invalidateFrbGamesForBank(bankId);
            deliveredNotes.add(targetForNote);
        }
        LOG(subCasino.getId() + "-" + subCasino.getName() + "\n");
    }
    LOG("\nSystems: " + targetSystems.size());
    LOG("\nBanks: " + targetBanksNumber);
    LOG("\n\nInvalidated for Slaves (" + deliveredNotes.size() + "):\n\n" + Joiner.on("\n").join(deliveredNotes));
    LOG("</pre>");

%>
<%!
    PrintWriter writer;

    private ArrayList<Long> findTargetBanks(SubCasino subCasino) {
        BankInfoCache bankInfoCache = BankInfoCache.getInstance();
        ArrayList<Long> targetBanks = new ArrayList<>();
        for (Long bankId : subCasino.getBankIds()) {
            BankInfo bankInfo = bankInfoCache.getBankInfo(bankId);
            if (bankInfo == null || !bankInfo.isEnabled()) continue;
            if (StringUtils.isTrimmedEmpty(bankInfo.getExternalBankId())) continue;
            if (!isSlaveBank(bankInfo)) continue;
            if (bankInfo.getCustomerSettingsHtml5Pc().contains("germany")) continue;
            targetBanks.add(bankId);
        }
        return targetBanks;
    }

    private boolean isSlaveBank(BankInfo bankInfo) {
        if (bankInfo.getMasterBankId() == null) {
            return false;
        } else {
            BankInfo masterBank = BankInfoCache.getInstance().getBankInfo(bankInfo.getMasterBankId());
            return bankInfo.getMasterBankId() != bankInfo.getId() && bankInfo.getSubCasinoId() == masterBank.getSubCasinoId();
        }
    }

    private void LOG(String str) {
        writer.write(str);
        writer.flush();
    }

%>