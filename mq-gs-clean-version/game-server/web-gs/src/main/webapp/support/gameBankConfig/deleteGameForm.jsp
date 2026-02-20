<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<SCRIPT>

    function isDigit(event, input, allow_point) {
        var key, ctrl;

        if (window.event) {
            key = window.event.keyCode;
            ctrl = window.event.ctrlKey;
        } else {
            key = event.which;
            ctrl = event.ctrlKey;
        }

        if ((key == 46) || (key == 8) || (key == 9)) return true; // delete, backspace, TAB
        if ((key >= 48) && (key <= 57)) return true;    // 0..9
        if (((key == 86) || (key == 67) || (key == 88)) && ctrl) return true; // X C V
        if (key == 190 && allow_point) {
            var points = input.value.split('.');
            if (points.length <= 1 && points[0] != '') return true;
        }


        return false;
    }
</SCRIPT>


<HTML>
<HEAD>
    <TITLE> Game Remover </TITLE>
</HEAD>
<BODY>

<FORM ACTION="removeGame.jsp" METHOD="POST">
    <TABLE id="removerTable" border="0">
        <TD>Список ID игр для удаления:</TD>
        <TD><INPUT id="id_game_ids" style="width: 800px" type="text" name="game_ids" value=""/></TD>
        <TR>
            <TD>Со следующих ID банков:</TD>
            <TD><INPUT id="id_bank_ids" style="width: 800px" type="text" name="bank_ids" value=""/></TD>
        <TR>
    </TABLE>

    <INPUT type="checkbox" name="check_template"> Удалить темплейты
    <HR>
    <INPUT type="checkbox" name="check_full"> Полное удаление игр со всех субказино и банков.

    <BR><BR>
    <INPUT style="width: 120px" type="submit" value="ОК"/>
</FORM>

</BODY>
</HTML>