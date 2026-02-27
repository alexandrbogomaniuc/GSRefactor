<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="com.dgphoenix.casino.common.cache.*" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.Coin" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.Limit" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.currency.Currency" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.*" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.util.*" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.google.common.collect.FluentIterable" %>
<%@ page import="com.google.common.base.Splitter" %>

<%!
    private static String resolveSpGameProcessorClassName() {
        String absClassName = "com.abs.casino.gs.singlegames.tools.cbservtools.SPGameProcessor";
        try {
            Class.forName(absClassName);
            return absClassName;
        } catch (ClassNotFoundException ignore) {
            return "com.abs.casino.gs.singlegames.tools.cbservtools.SPGameProcessor";
        }
    }

    long[] ACS_GAMES = {2, 3, 18, 20, 21, 33, 41, 47, 52, 80, 88, 91, 104, 122, 133, 134, 136, 143, 148, 155, 156, 249,
            263, 268, 355, 172, 151, 152, 212, 213, 255, 560, 561, 562};


    String servletTypes[] = {"", "Mobile", "Android", "WindowsPhone"};
    String titleTypes[] = {"", "Mobile", "Android", "Windows Phone"};
    String platformNames[] = {"", "MOBILE", "ANDROID", "WINDOWSPHONE"};
    String deviceTypes[] = {"PC", "IOSMOBILE", "ANDROID", "WINDOWSPHONE"};

    class UnjParams {
        public long baseAmount;
        public double pcrp;
        public double bcrp;
        public String extraId;

        public UnjParams(String extraId, long baseAmount, double pcrp, double bcrp) {
            this.baseAmount = baseAmount;
            this.pcrp = pcrp;
            this.bcrp = bcrp;
            this.extraId = extraId;
        }

        public String toString() {
            return "{" + "name: " + extraId + ", base: " + baseAmount + ", pcrp: " + pcrp + ", bcrp: " + bcrp + "}";
        }
    }

    class Game {
        PrintWriter writer = null;
        String spGameProcessor = resolveSpGameProcessorClassName();
        String swfLocation = null;
        String gameControllerClass = null;
        String servletStartWith = "";
        String servletEndWith = "";

        String name = "";
        String title = null;
        long[] gameIds = new long[4];
        String defcoin = "0";
        boolean acs = false;
        boolean isEnabled = false;
        boolean isTest = false;
        String[] models = null;
        Integer currentModel = null;

        String payout_percent = "0.97";

        List<Coin> coins = null;
        Limit limit;

        boolean isJackPot = false;
        Double pcrp = 0.0;
        Double bcrp = 0.0;
        String jackpotMultiplier = null;
        String jpWinLimit = null;


        String isUnj = "FALSE";
        int unjLinkedGameId = 0;
        double totalContribution = 0.00;
        HashMap<String, UnjParams> mapUnjParams = new HashMap<String, UnjParams>(); // Name -> UNJ

        String cdnUrl = null, cdnUrlToGo = null;
        String[] rouletteLimits = null;

        boolean isJackPot3 = false;
        int jackpot3BankCount = 0;
        float[] arrayJP3Percent = {0.0f, 0.0f, 0.0f};
        long[] arrayJP3StartMin = {0, 0, 0};
        long[] arrayJP3StartMax = {0, 0, 0};

        GameType type = GameType.SP;
        GameGroup group = GameGroup.TABLE;
        GameVariableType varType = GameVariableType.LIMIT;
        ArrayList<String> onlyFlashProperties = null;
        HashMap<String, String> additionalProperties = null;
        RoundFinishedHelper roundFinishedHelper = null;
        String[] strCheckIDs;

        Html5PcVersionMode html5VersionMode = null;
        String unifiedLocation = null;

        String additionalFlashVars = null;
        String pdfRulesName = null;

        Currency currency = null;

        String repositoryFile = null;

        String isDevelopmentKey = "";
        String endRoundSignature = null;

        boolean isFRB = false;
        int frbCoin = -1;
        String gameTesting = null;

        String maxWin = null;
        String rtp = null;
        String possibleLines = null;
        String possibleBetPerLines = null;
        String linesCount = null;
        String defaultNumLines = null;
        String defaultBetPerLine = null;

        String cdn_support = null;
        String lga_approved = null;
        String gameWithProgress = null;
        String gameWithDoubleUp = null;

        String templateJPMultiplier = null;
        String pcr_def = null;
        String[] sideJPidsPC = null;
        String[] fakeIdForPC = null;
        String jackpot_frequency = null;
        String jackpot_name = null;

        List<String>[] langugages = new List[4];

        void setRepositoryFile(String repositoryFile) {
            this.repositoryFile = repositoryFile;
        }

        void setDevelopmentKey(String isDevelopment) {
            this.isDevelopmentKey = isDevelopment;
        }

        void setEndRoundSignature(String endRoundSignature) {
            this.endRoundSignature = endRoundSignature;
        }

        void setTitle(String title) {
            this.title = title;
        }

        void setCheckIDs(String[] strCheckIDs) {
            this.strCheckIDs = strCheckIDs;
        }

        void setPayoutPercent(String str_PayoutPercent) {
            this.payout_percent = str_PayoutPercent;
        }

        void setCoins(List<Coin> coins) {
            this.coins = ((coins == null || coins.isEmpty()) ? null : coins);
        }

        void setSwfLocation(String swfLocation) {
            this.swfLocation = swfLocation;
        }

        void setGameControllerClass(String gameControllerClass) {
            this.gameControllerClass = gameControllerClass;
        }

        void setServletName(String servlet) {
            if (servlet != null) {
                String[] servletInfo = servlet.split("\\.");
                this.servletStartWith = servletInfo[0];
                this.servletEndWith = servletInfo[1];
            } else {
                this.servletStartWith = null;
                this.servletEndWith = null;
            }
        }

        void setMaxWin(String maxWin) {
            this.maxWin = maxWin;
        }

        void setRTP(String rtp) {
            this.rtp = rtp;
        }

        void setPossibleLines(String possibleLines) {
            this.possibleLines = possibleLines;
        }

        void setPossibleBetPerLines(String possibleBetPerLines) {
            this.possibleBetPerLines = possibleBetPerLines;
        }

        void setLinesCount(String linesCount) {
            this.linesCount = linesCount;
        }

        void setDefaultNumLines(String defaultNumLines) {
            this.defaultNumLines = defaultNumLines;
        }

        void setDefaultBetPerLine(String defaultBetPerLine) {
            this.defaultBetPerLine = defaultBetPerLine;
        }

        void setCDNSupport(String reqCDNSupport) {
            this.cdn_support = reqCDNSupport;
        }

        void setLGA_Approved(String reqLGA_Approved) {
            this.lga_approved = reqLGA_Approved;
        }

        void setGameWithProgress(String reqGameWithProgress) {
            this.gameWithProgress = reqGameWithProgress;
        }

        void setGameWithDoubleUp(String reqGameWithDoubleUp) {
            this.gameWithDoubleUp = reqGameWithDoubleUp;
        }

        void setTemplateJPMultiplier(String reqTemplateJPMultiplier) {
            this.templateJPMultiplier = reqTemplateJPMultiplier;
        }

        void setPCRDef(String reqPCRDef) {
            this.pcr_def = reqPCRDef;
        }

        void setSideJPids(String[] reqSideJPidsPC) {
            this.sideJPidsPC = reqSideJPidsPC;
        }

        void setFakeIdFor(String[] reqFakeIdForPC) {
            this.fakeIdForPC = reqFakeIdForPC;
        }

        void setJPName(String reqJPName) {
            this.jackpot_name = reqJPName;
        }

        void setJPFrequency(String reqJPFrequency) {
            this.jackpot_frequency = reqJPFrequency;
        }

        void setPdfRulesName(String pdfRulesName) {
            this.pdfRulesName = pdfRulesName;
        }

        void setAdditionalFlashVars(String additionalFlashVars) {
            this.additionalFlashVars = additionalFlashVars;
        }

        Game(String name) {
            this.name = name;
            if (additionalProperties == null) additionalProperties = new HashMap<String, String>();
            if (onlyFlashProperties == null) onlyFlashProperties = new ArrayList<String>();
        }

        long parseGameID(String platform) {
            if (platform == null || platform.equals("null")) return -1;
            else return Long.parseLong(platform);
        }

        void setIds(String str_pc, String str_mobile, String str_android, String str_windowsPhone) {
            if (str_pc != null && !str_pc.equals("")) gameIds[0] = parseGameID(str_pc);
            else gameIds[0] = -1;
            if (str_mobile != null && !str_mobile.equals("")) gameIds[1] = parseGameID(str_mobile);
            else gameIds[1] = -1;
            if (str_android != null && !str_android.equals("")) gameIds[2] = parseGameID(str_android);
            else gameIds[2] = -1;
            if (str_windowsPhone != null && !str_windowsPhone.equals("")) gameIds[3] = parseGameID(str_windowsPhone);
            else gameIds[3] = -1;
        }

        void setType(String str_gameType, String str_groupType, String str_varType) {
            type = str_gameType.equals("SP") ? GameType.SP : GameType.MP;
            varType = str_varType.equals("COIN") ? GameVariableType.COIN : GameVariableType.LIMIT;

            if (str_groupType.contains("SLOTS")) {
                group = GameGroup.SLOTS;
            } else if (str_groupType.contains("TABLE")) {
                group = GameGroup.TABLE;
            } else if (str_groupType.contains("KENO")) {
                group = GameGroup.KENO;
            } else if (str_groupType.contains("VIDEOPOKER")) {
                group = GameGroup.VIDEOPOKER;
            } else if (str_groupType.contains("SOFT_GAMES")) {
                group = GameGroup.SOFT_GAMES;
            } else if (str_groupType.contains("PYRAMID_POKER")) {
                group = GameGroup.PYRAMID_POKER;
            } else if (str_groupType.contains("SOFT_GAME_ARCADE")) {
                group = GameGroup.SOFT_GAME_ARCADE;
            } else if (str_groupType.contains("MULTIHAND_POKER")) {
                group = GameGroup.MULTIHAND_POKER;
            } else if (str_groupType.contains("MULTISTACK_POKER")) {
                group = GameGroup.MULTISTACK_POKER;
            } else if (str_groupType.contains("LIVE")) {
                group = GameGroup.LIVE;
            } else if (str_groupType.contains("ACTION_GAMES")) {
                group = GameGroup.ACTION_GAMES;
            }
        }

        void setRoundFinishedHelper(String strRoundFinishedHelper) {
            if (strRoundFinishedHelper.equals("NULL")) {
                roundFinishedHelper = null;
            } else {
                roundFinishedHelper = RoundFinishedHelper.valueOf(strRoundFinishedHelper);
            }
        }

        void setHtml5VersionMode(String strHtml5VersionMode, String strUnifiedLocation) {
            if (strHtml5VersionMode == null || strHtml5VersionMode.equals("NULL")) {
                html5VersionMode = null;
            } else {
                html5VersionMode = Html5PcVersionMode.valueOf(strHtml5VersionMode);
            }

            if (strUnifiedLocation != null && !strUnifiedLocation.equals("null")) {
                unifiedLocation = strUnifiedLocation;
            }
        }

        void setDefCoin(String strDefCoin) {
            if (strDefCoin == null || strDefCoin.contains("null") || strDefCoin.equals("")) defcoin = null;
            else defcoin = strDefCoin;
        }

        public void setFRB(String strIsFRB, String strFRBCoin) {
            isFRB = (strIsFRB.equals("TRUE"));
            if (isFRB) {
                if (strFRBCoin != null && !strFRBCoin.equals("") && !strFRBCoin.equals("null")) frbCoin = Integer.parseInt(strFRBCoin);
                else frbCoin = -1;
            }
        }

        public void setGameTesting(String strGameTesting) {
            if (strGameTesting != null && !strGameTesting.equals("NULL"))
                gameTesting = strGameTesting;
        }

        void setFlags(String strIsKeyAcs, String strIsEnabled, String strIsJackpot) {
            acs = (strIsKeyAcs != null && strIsKeyAcs.equals("TRUE"));
            isEnabled = (strIsEnabled != null && strIsEnabled.equals("TRUE"));
            isJackPot = (strIsJackpot != null && strIsJackpot.equals("TRUE"));
            currentModel = null;

            // Check games without ACS with few models
            BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameIds[0]);
            if (template != null) {
                String strModels = template.getDefaultGameInfo().getProperty("POSSIBLE_MODELS");

                if (strModels != null && acs) {
                    models = strModels.split("\\|");
                    if (payout_percent != null) {
                        double payoutPercentValue = Double.parseDouble(payout_percent) * 100.0;
                        for (currentModel = models.length - 1; currentModel >= 0; currentModel--) {
                            double modelValue = Double.parseDouble(models[currentModel]);
                            if (modelValue <= payoutPercentValue) {
                                break;
                            }
                        }
                    }
                }
            }
        }

        void setLimit(String strLimitMin, String strLimitMax) {
            if (strLimitMin == null || strLimitMin.isEmpty() || strLimitMax == null || strLimitMax.isEmpty()) limit = null;
                //else limit = new Limit(0, Integer.parseInt(strLimitMin), Integer.parseInt(strLimitMax));
            else limit = Limit.valueOf(Integer.parseInt(strLimitMin), Integer.parseInt(strLimitMax));
        }

        void setLimit(Limit limit) {
            this.limit = limit;
        }

        void setJackPotInfo(String multiplier, String pcrp, String bcrp, String jpWinLimit) {
            if (isJackPot) {
                this.jackpotMultiplier = multiplier;
                this.pcrp = Double.parseDouble(pcrp);
                this.bcrp = Double.parseDouble(bcrp);
                this.jpWinLimit = jpWinLimit;
            }
        }

        String getUNJValue(String value, int prefferedIndex, String defaultValue) {
            String[] arrayValue = value.split(",");

            if (arrayValue.length == 2) {
                return arrayValue[prefferedIndex];
            } else {
                return (defaultValue == null) ? arrayValue[0] : defaultValue;
            }
        }

        void setUnjInfo(String strIsUnj, String strTotalContribution,
                        String unjName_1, String unjName_2, String unjName_3, String unjName_4,
                        String unjBaseAmount_1, String unjBaseAmount_2, String unjBaseAmount_3, String unjBaseAmount_4,
                        String unjPcrp_1, String unjPcrp_2, String unjPcrp_3, String unjPcrp_4,
                        String unjBcrp_1, String unjBcrp_2, String unjBcrp_3, String unjBcrp_4) {
            isUnj = strIsUnj;

            if (isUnj.equalsIgnoreCase("true")) {
                totalContribution = Double.parseDouble(strTotalContribution);

                if (unjBaseAmount_1 != null) addUnjJackpot(unjName_1, unjBaseAmount_1, unjPcrp_1, unjBcrp_1);
                if (unjBaseAmount_2 != null) addUnjJackpot(unjName_2, unjBaseAmount_2, unjPcrp_2, unjBcrp_2);
                if (unjBaseAmount_3 != null) addUnjJackpot(unjName_3, unjBaseAmount_3, unjPcrp_3, unjBcrp_3);
                if (unjBaseAmount_4 != null) addUnjJackpot(unjName_4, unjBaseAmount_4, unjPcrp_4, unjBcrp_4);
            }
        }

        void setUnjLinkedGame(String gameId) {
            unjLinkedGameId = Integer.parseInt(gameId);
        }

        void addUnjJackpot(String extraId, String strBaseAmount, String strPcrp, String strBcrp) {
            if (isUnj.equalsIgnoreCase("true")) {
                if (extraId == null) extraId = "";
                UnjParams unjParams = new UnjParams(extraId, Integer.parseInt(strBaseAmount), Double.parseDouble(strPcrp), Double.parseDouble(strBcrp));
                mapUnjParams.put(extraId, unjParams);
            }
        }

        void setLanguages(String[] strLanguages) {
            for (int i = 0; i < strLanguages.length; i++) {
                if (strLanguages[i] != null && !strLanguages[i].isEmpty()) {
                    langugages[i] = Arrays.asList(strLanguages[i].replaceAll(" ", "").split(","));
                } else {
                    langugages[i] = Arrays.asList("en");
                }
            }
        }

        public void setJackpot3(String strIsJackpot3) {
            isJackPot3 = strIsJackpot3.equals("TRUE");
        }

        public void setJackpot3ArrayPercent(String[] strArrayJp3percent) {
            if (strArrayJp3percent == null) return;
            for (int i = 0; i < strArrayJp3percent.length; i++) {
                if (strArrayJp3percent[i].equals("")) break;
                arrayJP3Percent[i] = Float.parseFloat(strArrayJp3percent[i]);
                jackpot3BankCount += 1;
            }
        }

        public void setJackpot3ArrayMin(String[] strArrayJp3startMin) {
            if (strArrayJp3startMin == null) return;
            for (int i = 0; i < jackpot3BankCount; i++) {
                if (strArrayJp3startMin[i].equals("")) arrayJP3StartMin[i] = 0;
                else arrayJP3StartMin[i] = Long.parseLong(strArrayJp3startMin[i]);
            }
        }

        public void setJackpot3ArrayMax(String[] strArrayJp3startMax) {
            if (strArrayJp3startMax == null) return;
            for (int i = 0; i < jackpot3BankCount; i++) {
                if (strArrayJp3startMax[i].equals("")) arrayJP3StartMax[i] = Long.parseLong(strArrayJp3startMax[i]);
                else arrayJP3StartMax[i] = Long.parseLong(strArrayJp3startMax[i]);
            }
        }

        public void setCDN_URL(String cdn_url) {
            cdnUrl = cdn_url;
        }

        public void setCDN_URL_ToGo(String cdn_urlToGo) {
            cdnUrlToGo = cdn_urlToGo;
        }

        public void setChipValues(String chipValues) {
            if (chipValues != null) {
                additionalProperties.put("CHIPVALUES", chipValues);
            } else {
                additionalProperties.remove("CHIPVALUES");
            }
        }

        public void setRouletteLimits(BaseGameInfo etalonGame) {
            if (etalonGame == null) {
                rouletteLimits = null;
            } else {
                rouletteLimits = new String[8];

                rouletteLimits[0] = etalonGame.getProperty("MAX_BET_1");
                rouletteLimits[1] = etalonGame.getProperty("MAX_BET_2");
                rouletteLimits[2] = etalonGame.getProperty("MAX_BET_3");
                rouletteLimits[3] = etalonGame.getProperty("MAX_BET_4");
                rouletteLimits[4] = etalonGame.getProperty("MAX_BET_5");
                rouletteLimits[5] = etalonGame.getProperty("MAX_BET_6");
                rouletteLimits[6] = etalonGame.getProperty("MAX_BET_12");
                rouletteLimits[7] = etalonGame.getProperty("MAX_BET_18");

                if (rouletteLimits[0] != null) additionalProperties.put("MAX_BET_1", rouletteLimits[0]);
                if (rouletteLimits[1] != null) additionalProperties.put("MAX_BET_2", rouletteLimits[1]);
                if (rouletteLimits[2] != null) additionalProperties.put("MAX_BET_3", rouletteLimits[2]);
                if (rouletteLimits[3] != null) additionalProperties.put("MAX_BET_4", rouletteLimits[3]);
                if (rouletteLimits[4] != null) additionalProperties.put("MAX_BET_5", rouletteLimits[4]);
                if (rouletteLimits[5] != null) additionalProperties.put("MAX_BET_6", rouletteLimits[5]);
                if (rouletteLimits[6] != null) additionalProperties.put("MAX_BET_12", rouletteLimits[6]);
                if (rouletteLimits[7] != null) additionalProperties.put("MAX_BET_18", rouletteLimits[7]);
            }
        }

        void addToProperties(String key, String value, String onlyFlash) {
            if (key.equals("")) return;
            additionalProperties.put(key, value);

            if (onlyFlash != null && onlyFlash.equals("on"))
                onlyFlashProperties.add(key);
        }

        void removeBadProperties(BaseGameInfoTemplate game_template, int i) {
            String property = game_template.getDefaultGameInfo().getProperty(deviceTypes[i]);

            if (property != null) {
                writer.write("<b>BAD DEVICE TYPE: </b>" + deviceTypes[i] + "=" + property + "<br>");
                game_template.getDefaultGameInfo().removeProperty(deviceTypes[i]);
                if (i == 0) game_template.getDefaultGameInfo().removeProperty("KEY_PLAYER_DEVICE_TYPE");
            }
        }

        String getPayoutPercent() {
            return payout_percent;
        }

        String getDefCoin() {
            return defcoin;
        }

        String getAcs() {
            return (acs ? "TRUE" : "FALSE");
        }

        String getModels() {
            return models != null ? Arrays.toString(models) : null;
        }

        String getCurrentModel() {
            return (currentModel != null ? String.valueOf(currentModel) : null);
        }

        String getEnabled() {
            return (isEnabled ? "TRUE" : "FALSE");
        }

        String getTitle(int index) {
            if (title == null || title.isEmpty()) return null;
            return (title + " " + titleTypes[index]).trim();
        }

        String getFullName(int platform_index) {
            return (name == null) ? null : name + platformNames[platform_index];
        }

        String getServlet(int platform_index) {
            return (servletStartWith == null) ? null : (servletStartWith + servletTypes[platform_index] + "." + ((platform_index == 0) ? servletEndWith : "game"));
        }

        String getRepositoryFile() {
            return repositoryFile;
        }

        String getJackpot3Value() {
            return isJackPot3 ? "TRUE" : "FALSE";
        }

        String getSWFLocation() {
            return swfLocation;
        }

        String getGameControllerClass() {
            return gameControllerClass;
        }

        String getEndRoundSignature() {
            return endRoundSignature;
        }

        boolean isMustBeACS(long gameId) {
            for (long acs_game : ACS_GAMES)
                if (acs_game == gameId) return true;

            return false;
        }

        Map getProperties(int game_index, boolean isDefaultCurrency) {
            Map<String, String> properties = new HashMap<String, String>();

            properties.put("ISENABLED", getEnabled());

            boolean isACS = isMustBeACS(gameIds[game_index]) || acs;
            if (isACS && !name.startsWith("AAMS")) {
                properties.put("KEY_ACS_ENABLED", isACS ? "TRUE" : "FALSE");

                if (payout_percent != null && !payout_percent.trim().equalsIgnoreCase("null")) {
                    properties.put("PAYOUT_PERCENT", getPayoutPercent());

                    if (getCurrentModel() != null) {
                        properties.put("CURRENT_MODEL", getCurrentModel());
                    }
                }
            }

            if (defcoin != null)
                properties.put("DEFCOIN", getDefCoin());

            if (isDefaultCurrency && gameTesting != null)
                properties.put("GAME_TESTING", gameTesting);

            if (isJackPot) {
                if (jackpotMultiplier != null) {
                    properties.put("JACKPOT_MULTIPLIER", jackpotMultiplier);
                }

                if (jpWinLimit != null) {
                    properties.put("JACKPOT_WIN_LIMIT", jpWinLimit);
                }
            }

            if (game_index == 0 && cdnUrl != null) properties.put("CDN_URL", cdnUrl);
            else if (game_index > 0 && cdnUrlToGo != null) properties.put("CDN_URL", cdnUrlToGo);

            if (additionalProperties != null) {
                for (String key : additionalProperties.keySet()) {
                    if (game_index == 0 || !onlyFlashProperties.contains(key))
                        properties.put(key, additionalProperties.get(key));
                }
            }

            properties.remove(BaseGameConstants.KEY_FRB_COIN);

            if (frbCoin != -1 && varType == GameVariableType.COIN) {
                properties.put(BaseGameConstants.KEY_FRB_COIN, String.valueOf(frbCoin));
            }

            return properties;
        }

        public String exportGameIds() {
            String result = "";
            for (int i = 0; i < gameIds.length; i++) {
                if (gameIds[i] != -1) result += gameIds[i];
                if (i < gameIds.length - 1) result += ", ";
            }
            return result;
        }

        public boolean isChecked(int i) {
            return (gameIds[i] != -1 && strCheckIDs[i] != null && strCheckIDs[i].equals("on"));
        }

        public boolean isSingleGameId() {
            BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameIds[0]);
            return template.isSingleGameIdForAllPlatforms();
        }
    }
%>
