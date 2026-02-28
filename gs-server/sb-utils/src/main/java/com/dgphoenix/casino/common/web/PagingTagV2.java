package com.abs.casino.common.web;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

public class PagingTagV2 extends TagSupport {

    private String formName;
    private String styleClass = "pageCounter";
    private String styleSelectedClass = "pageCounterSelected";

    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    public String getStyleSelectedClass() {
        return styleSelectedClass;
    }

    public void setStyleSelectedClass(String styleSelectedClass) {
        this.styleSelectedClass = styleSelectedClass;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public int doStartTag() throws JspException {
        IPageableForm form = (IPageableForm) pageContext.findAttribute(
                formName);
        int count = form.getCount();
        int page = form.getPage();

        if (count < 1) {
            return SKIP_BODY;
        }

        int lastPage = form.getLastPage();

        if (lastPage < 2) {
            return SKIP_BODY;
        }

        if ((page - 1) * form.getItemsPerPage() > count) {
            page = lastPage;
        }

        int currentPageBlock = (int) Math.ceil(
                (double) page / form.getPagesPerPage());
        int startingPage = ((currentPageBlock - 1) * form.getPagesPerPage()) +
                1;

        StringBuilder sb = new StringBuilder();

        sb.append("<table>");
        sb.append("<tr>");
        sb.append("<td class=\"arrow\">");

        if (currentPageBlock > 1) {
            addLink(sb, startingPage - 1, "<", "arrowLeft");
            sb.append("&nbsp;");
        }

        sb.append("</td>");
        sb.append("<td>");

        int currentPage = startingPage;
        sb.append("<span class=\"scrollCenter\">");
        do {
            if (currentPage > startingPage) {
                sb.append("&nbsp;");
            }
            if (currentPage == page) {
                sb.append("<span class='" + styleSelectedClass + "'>");
                sb.append(currentPage);
                sb.append("</span>");
            } else {
                addLink(sb, currentPage, String.valueOf(currentPage));
            }
            currentPage++;
        } while (currentPage <= lastPage &&
                currentPage <= startingPage + form.getPagesPerPage());

        sb.append("</span>");

        sb.append("</td>");
        sb.append("<td class=\"arrow\">");

        if (startingPage + form.getPagesPerPage() - 1 < lastPage) {
            sb.append("&nbsp;");
            addLink(sb, startingPage + form.getPagesPerPage(), ">", "arrowRight");
        }

        sb.append("</td>");
        sb.append("</tr>");
        sb.append("</table>");

        try {
            pageContext.getOut().write(sb.toString());
        } catch (IOException e) {
            throw new JspException("Error writing to jsp", e);
        }

        return SKIP_BODY;
    }

    private void addLink(StringBuilder sb, int page, String label) {
        addLink(sb, page, label, null);
    }
    private void addLink(StringBuilder sb, int page, String label, String styleClass) {
        sb.append("<span class='" + getStyleClass() + "'>");
        sb.append("<a href='javascript:void(0);' onClick='javascript:doPaging(" + page + ");return false;'");
        if(styleClass != null) {
            sb.append(" class=\""+styleClass+"\"");
        }
        sb.append(">");
        sb.append(label);
        sb.append("</a>");
        sb.append("</span>");
    }

}
