<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.dgphoenix.casino.common.cache.SubCasinoCache" %>
<%@ page import="java.util.*" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.SubCasino" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.util.property.EnumProperty" %>

<%
    String filterProperty = request.getParameter("filterProperty");
%>

<script type="text/javascript" src="/tools/js/jquery.min.js"></script>

<script>

    function sortOptions(selectId) {
        var $options = $("#" + selectId + " option");
        $options.detach().sort(function (a, b) {
            var at = $(a).text();
            var bt = $(b).text();
            return (at > bt) ? 1 : ((at < bt) ? -1 : 0);
        }).appendTo("#" + selectId);
    }

    var banksListUrl = "/support/cache/bank/common/banksList.jsp";
    var addAllEntry = false;
    var filterProperty = '<%=filterProperty%>';
    function getBanks(subcasinoId) {
        $.ajax({
            type: "GET",
            url: banksListUrl,
            data: {
                "subcasinoId": subcasinoId,
                "filterProperty": filterProperty
            },
            success: function (data) {
                $("#bankId").html(data);
                sortOptions("bankId");
                if (addAllEntry) {
                    $("#bankId").prepend('<option value="-1">All</option>');
                }
                $("#bankId").val(0);
            },
            error: function (xhr, status, error) {
                $("#bankId").html(xhr.responseText);
            }
        });
        return true;
    }
</script>

<%
    long subcasinoId = 0;
    String subcasino = request.getParameter("subcasinoId");
    if (StringUtils.isNotEmpty(subcasino)) {
        subcasinoId = Long.valueOf(subcasino);
    }
    Set<Long> setOfKeys = SubCasinoCache.getInstance().getAllObjects().keySet();
    List<Long> ids = new ArrayList<Long>(setOfKeys);
    Collections.sort(ids);
%>
<table>
    <tr>
        <td>SubCasino id</td>
        <td>Bank id</td>
    </tr>
    <tr>
        <td>
            <select name="subcasinoId" id="subcasinoId"><%
                for (Long id : ids) {
                    if (id != null && id != 0) {
                        final SubCasino subCasino = SubCasinoCache.getInstance().get(id);
                        String sName = subCasino.getName();
                        if (StringUtils.isEmpty(sName)) {
                            Long bankId = SubCasinoCache.getInstance().getDefaultBankId(id);
                            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
                            if (bankInfo != null) {
                                sName = bankInfo.getExternalBankIdDescription();
                            }
                        }

                        List<Long> bankIds = SubCasinoCache.getInstance().getBankIds(id);
                        if (bankIds != null) {
                            Collections.sort(bankIds);
                        }

                        String filter = "";
                        boolean isEnumProp = false;
                        if (StringUtils.isNotBlank(filterProperty)) {
                            isEnumProp = BankInfo.class.getDeclaredField("KEY_" + filterProperty).isAnnotationPresent(EnumProperty.class);
                            if (isEnumProp) {
                                filter = "NONE";
                            }
                        }
                        boolean addSubCasino = false;
                        for (Long bid : bankIds) {
                            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bid);
                            if (bankInfo != null) {
                                String prop = StringUtils.isNotBlank(filterProperty) ? bankInfo.getStringProperty(filterProperty) : "";
                                if (isEnumProp && StringUtils.isBlank(prop)) {
                                    prop = "NONE";
                                }
                                if (prop == null) {
                                    prop = "";
                                }
                                if (StringUtils.isBlank(filterProperty) || !filter.equals(prop)) {
                                    addSubCasino = true;
                                    break;
                                }
                            }
                        }
                        if (addSubCasino) {
            %>
                <option value="<%=id%>" <%=(subcasinoId == id ? "selected" : "")%>><%=sName%>
                </option>
                <%
                            }
                        }
                    }
                %></select>
        </td>
        <td><select name="bankId" id="bankId"></select></td>
    </tr>
</table>

<script>
    $("#subcasinoId").change(function () {
        getBanks($("option:selected", this)[0].value);
        return true;
    });
    $(document).ready(function () {
        sortOptions("subcasinoId");
        $("#subcasinoId").val(0).change();
    });
</script>
