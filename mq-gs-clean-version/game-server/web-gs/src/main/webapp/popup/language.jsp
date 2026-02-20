<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%!
    public static String getMessage(String lang, String d) {
        String fPart = null;
        String sPart = null;

        if (lang.equalsIgnoreCase("fr")) {
            fPart = new String("Vous jouez en mode TEST!");
            sPart = new String("Tous les gains sont nuls et annulé.");
        }

        if (lang.equalsIgnoreCase("th")) {
            fPart = new String("คุณกำลังเล่นอยู่ในโหมดทดลอง!");
            sPart = new String("รางวัลที่ได้มาทั้งหมดถือเป็นโมฆะ");
        }

        if (lang.equalsIgnoreCase("vi")) {
            fPart = new String("Bạn đang chơi ở chế độ THỬ NGHIỆM!");
            sPart = new String("Tất cả tiền thắng cược sẽ bị vô hiệu lực.");
        }

        if (lang.equalsIgnoreCase("el")) {
            fPart = new String("Παίζετε σε ΔΟΚΙΜΑΣΤΙΚΗ λειτουργία!");
            sPart = new String("Όλα τα κέρδη είναι μηδενικά και άκυρα.");
        }

        if (lang.equalsIgnoreCase("jp")) {
            fPart = new String("これはテストモードです。");
            sPart = new String("勝ち得点は有効ではありません。");
        }

        if (lang.equalsIgnoreCase("nl")) {
            fPart = new String("U speelt nu in TEST mode!");
            sPart = new String("Al wat u gewonnen heeft zijn niet geldig.");
        }

        if (lang.equalsIgnoreCase("no")) {
            fPart = new String("Du spiller i TEST-modus.");
            sPart = new String("Det vil si at alle seirer og gevinster er ugyldige.");
        }

        if (lang.equalsIgnoreCase("se")) {
            fPart = new String("Du spelar i TEST-läge!");
            sPart = new String("Alla vinster är tomma och ogiltiga.");
        }

        if (lang.equalsIgnoreCase("tr")) {
            fPart = new String("DENEME modunda oynuyorsunuz.");
            sPart = new String("Butun kazanclar gecersiz ve hukumsuzdur.");
        }

        if (lang.equalsIgnoreCase("pl")) {
            fPart = new String("Grasz w trybie testowym!");
            sPart = new String("Wszystkie wygrane są zerowe i nieważne.");
        }

        if (lang.equalsIgnoreCase("ko")) {
            fPart = new String("현재 테스트 모드로 플레이 중입니다!");
            sPart = new String("모든 상금은 무효입니다.");
        }

        if (lang.equalsIgnoreCase("pt-eu")) {
            fPart = new String("Está a jogar em modo de TESTE!");
            sPart = new String("Todos os prémios são invalidos e serão cancelados.");
        }

        if (lang.equalsIgnoreCase("pt")) {
            fPart = new String("Você está jogando no modo de TESTE!");
            sPart = new String("Todas as vitórias são nulas");
        }

        if (lang.equalsIgnoreCase("de")) {
            fPart = new String("Sie spielen nun im TEST Modus!");
            sPart = new String("Alle Gewinne sind nichtig.");
        }

        if (lang.equalsIgnoreCase("bg")) {
            fPart = new String("Играете в ТЕСТ режим.");
            sPart = new String("Всички печалби са 0 и невалидни,");
        }

        if (lang.equalsIgnoreCase("dk")) {
            fPart = new String("Du spiller i TEST tilstand.");
            sPart = new String("Alle gevinster er ikke gældende.");
        }

        if (lang.equalsIgnoreCase("es")) {
            fPart = new String("¡Estás jugando el el modo TEST!");
            sPart = new String("Todas las ganancias son nulas e inválidas.");
        }

        if (lang.equalsIgnoreCase("it")) {
            fPart = new String("Stai giocando in modalita' TEST.");
            sPart = new String("Tutte le vincite sono nulle.");
        }

        if (lang.equalsIgnoreCase("id")) {
            fPart = new String("Anda sedang bermain dalam mode TEST!");
            sPart = new String("Semua kemenangan hanyalah sebagai contoh dan tidak dapat di klaim.");
        }

        if (lang.equalsIgnoreCase("hu")) {
            fPart = new String("Ön most próbajátékmódban van!");
            sPart = new String("Ez alatt az összes nyeremény érvénytelen, semmis.");
        }

        if (lang.equalsIgnoreCase("ro")) {
            fPart = new String("Acum joci în opţiunea de TEST!");
            sPart = new String("Toate câştigurile sunt nule şi inexistente.'");
        }

        if (lang.equalsIgnoreCase("cz")) {
            fPart = new String("Hrajete v režimu TEST!");
            sPart = new String("Všechny výhry jsou neplatné.");
        }

        if (lang.equalsIgnoreCase("sk")) {
            fPart = new String("Hráte v režime TEST!");
            sPart = new String("Všetky výhry sú neplatné.");
        }

        if (lang.equalsIgnoreCase("zh-cn")) {
            fPart = new String("您正在玩测试模式！");
            sPart = new String("所有的奖金都是无效的。");
        }

        if (lang.equalsIgnoreCase("zh")) {
            fPart = new String("您正在玩測試模式！");
            sPart = new String("所有的獎金都是無效的。");
        }

        if (lang.equalsIgnoreCase("ru")) {
            fPart = new String("Вы играете в ПРОБНОМ режиме!");
            sPart = new String("Весь выигрыш недействительный.");
        }

        if (lang.equalsIgnoreCase("fi")) {
            fPart = new String("Pelaat TESTI-tilassa!");
            sPart = new String("Voitot eivät ole lunastettavissa.");
        }

        if (lang.equalsIgnoreCase("en") || (fPart == null && sPart == null)) {
            fPart = new String("You are playing in TEST mode!");
            sPart = new String("All winnings are null and void.");
        }

        return fPart + d + sPart;
    }
%>