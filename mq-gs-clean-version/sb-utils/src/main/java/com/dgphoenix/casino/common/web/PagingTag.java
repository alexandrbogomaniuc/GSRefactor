package com.dgphoenix.casino.common.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

public class PagingTag extends TagSupport {

    public static final String PAGE_ATTRIBUTE = "page";

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

        int lastPage = (int) Math.ceil((double) count / form.getItemsPerPage());

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

        if (currentPageBlock > 1) {
            addLink(sb, startingPage - 1, "<");
            sb.append("&nbsp;");
        }

        int currentPage = startingPage;

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

        if (startingPage + form.getPagesPerPage() - 1 < lastPage) {
            sb.append("&nbsp;");
            addLink(sb, startingPage + form.getPagesPerPage(), ">");
        }

        try {
            pageContext.getOut().write(sb.toString());
        } catch (IOException e) {
            throw new JspException("Error writing to jsp", e);
        }

        return SKIP_BODY;
    }

    private void addLink(StringBuilder sb, int page, String label) {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();

        sb.append("<span class='" + styleClass + "'>");
        sb.append("<a href=\"");
        sb.append(request.getAttribute("javax.servlet.forward.request_uri"));
        sb.append(getSessionStr(request, pageContext.getSession()));
        sb.append("?");
        sb.append(PAGE_ATTRIBUTE);
        sb.append("=");
        sb.append(page);
        sb.append("\">");
        sb.append(label);
        sb.append("</a>");
        sb.append("</span>");
    }

    public static String getSessionStr(
        HttpServletRequest request,
        HttpSession session) {
        if(request.getCookies() == null) {
            return ";jsessionid=" + session.getId();
        } else {
            return "";
        }
    }


}
