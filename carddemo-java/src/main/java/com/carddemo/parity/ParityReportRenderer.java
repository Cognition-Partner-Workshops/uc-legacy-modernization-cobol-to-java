package com.carddemo.parity;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

/**
 * Renders a self-contained HTML parity report from a {@link ParitySummary}.
 *
 * <p>The produced document inlines all CSS and never references external
 * assets, so it renders correctly when opened directly from disk.
 */
public final class ParityReportRenderer {

    /** Report location relative to the repository root, used for artifact links. */
    private static final String DEFAULT_LINK_BASE = "../../..";

    private static final Path DEFAULT_INPUT = Path.of("carddemo-java/target/parity/parity-summary.json");
    private static final Path DEFAULT_OUTPUT = Path.of("carddemo-java/target/parity/parity-report.html");

    private final String linkBase;

    public ParityReportRenderer() {
        this(DEFAULT_LINK_BASE);
    }

    /**
     * @param linkBase path prefix prepended to the repo-relative artifact paths,
     *                 so that links resolve from the report's own directory.
     */
    public ParityReportRenderer(String linkBase) {
        this.linkBase = linkBase == null ? "" : linkBase;
    }

    public static void main(String[] args) throws IOException {
        Path input = args.length > 0 ? Path.of(args[0]) : DEFAULT_INPUT;
        Path output = args.length > 1 ? Path.of(args[1]) : DEFAULT_OUTPUT;

        ParitySummary summary = new ParitySummaryReader().read(input);
        String html = new ParityReportRenderer().render(summary);

        Path parent = output.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(output, html, StandardCharsets.UTF_8);
        System.out.println("Wrote parity report: " + output.toAbsolutePath());
    }

    public String render(ParitySummary summary) {
        ParitySummary.Totals totals = summary.summary();
        List<AccountParity> accounts = summary.accounts();

        StringBuilder out = new StringBuilder(8192);
        out.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n")
                .append("<meta charset=\"utf-8\">\n")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n")
                .append("<title>CardDemo statement parity report</title>\n")
                .append("<style>\n").append(css()).append("</style>\n")
                .append("</head>\n<body>\n");

        out.append(header(summary, totals));
        out.append(cards(totals));
        out.append(table(accounts));

        out.append("<footer class=\"foot\">Self-contained report &mdash; no external assets or network requests.")
                .append("</footer>\n");
        out.append("</body>\n</html>\n");
        return out.toString();
    }

    private String header(ParitySummary summary, ParitySummary.Totals totals) {
        Verdict verdict = verdict(totals);
        return "<header class=\"head\">\n"
                + "<div class=\"head-main\">\n"
                + "<h1>Statement parity report</h1>\n"
                + "<dl class=\"meta\">\n"
                + "<dt>Source program</dt><dd>" + esc(orDash(summary.sourceProgram())) + "</dd>\n"
                + "<dt>Generated at</dt><dd>" + esc(orDash(summary.generatedAt())) + "</dd>\n"
                + "<dt>Accounts compared</dt><dd>" + totals.total() + "</dd>\n"
                + "</dl>\n</div>\n"
                + "<div class=\"verdict verdict-" + verdict.cssClass + "\">\n"
                + "<span class=\"verdict-label\">Overall verdict</span>\n"
                + "<strong class=\"verdict-value\">" + esc(verdict.text) + "</strong>\n"
                + "<span class=\"verdict-detail\">" + esc(verdict.detail) + "</span>\n"
                + "</div>\n</header>\n";
    }

    private String cards(ParitySummary.Totals t) {
        return "<section class=\"cards\" aria-label=\"Totals\">\n"
                + card("Total", t.total(), "total")
                + card("Match", t.match(), "match")
                + card("Diff", t.diff(), "diff")
                + card("Missing", t.missing(), "missing")
                + "</section>\n";
    }

    private String card(String label, int value, String kind) {
        return "<div class=\"card card-" + kind + "\">\n"
                + "<span class=\"card-label\">" + esc(label) + "</span>\n"
                + "<span class=\"card-value\">" + value + "</span>\n"
                + "</div>\n";
    }

    private String table(List<AccountParity> accounts) {
        StringBuilder sb = new StringBuilder();
        sb.append("<section class=\"table-wrap\">\n<h2>Per-account results</h2>\n");
        if (accounts.isEmpty()) {
            sb.append("<p class=\"empty\">No accounts were compared in this parity run.</p>\n</section>\n");
            return sb.toString();
        }
        sb.append("<table class=\"accounts\">\n<thead>\n<tr>")
                .append("<th>Account</th><th>Customer</th><th class=\"num\">Txns</th>")
                .append("<th class=\"num\">Total amount</th><th>Status</th><th>Diff details</th>")
                .append("<th>Artifacts</th></tr>\n</thead>\n<tbody>\n");
        for (AccountParity a : accounts) {
            sb.append(row(a));
        }
        sb.append("</tbody>\n</table>\n</section>\n");
        return sb.toString();
    }

    private String row(AccountParity a) {
        ParityStatus status = a.status();
        String kind = status.name().toLowerCase(Locale.ROOT);
        BigDecimal amount = a.totalAmount();
        boolean negative = amount.signum() < 0;

        return "<tr class=\"row-" + kind + "\">"
                + "<td class=\"acct\">" + esc(orDash(a.accountId())) + "</td>"
                + "<td>" + esc(orDash(a.customerName())) + "</td>"
                + "<td class=\"num\">" + a.transactionCount() + "</td>"
                + "<td class=\"num amount" + (negative ? " negative" : "") + "\">" + esc(currency(amount)) + "</td>"
                + "<td><span class=\"badge badge-" + kind + "\">" + status.name() + "</span></td>"
                + "<td class=\"diff\">" + diffDetails(a) + "</td>"
                + "<td class=\"links\">" + links(a) + "</td>"
                + "</tr>\n";
    }

    private String diffDetails(AccountParity a) {
        if (a.status() == ParityStatus.MATCH) {
            return "<span class=\"muted\">No differences</span>";
        }
        String first = a.firstDiffLine() == null
                ? "<span class=\"muted\">n/a</span>"
                : String.valueOf(a.firstDiffLine());
        String note = a.status() == ParityStatus.MISSING
                ? "<div class=\"diff-note\">One side of the comparison is absent</div>"
                : "";
        return "<div class=\"diff-line\"><span class=\"diff-key\">Differing lines</span>"
                + "<span class=\"diff-val\">" + a.diffLineCount() + "</span></div>"
                + "<div class=\"diff-line\"><span class=\"diff-key\">First diff line</span>"
                + "<span class=\"diff-val\">" + first + "</span></div>"
                + note;
    }

    private String links(AccountParity a) {
        return "<div class=\"link-group\"><span class=\"link-group-label\">COBOL</span>"
                + link("txt", a.cobolTextPath()) + link("html", a.cobolHtmlPath()) + "</div>"
                + "<div class=\"link-group\"><span class=\"link-group-label\">Java</span>"
                + link("txt", a.javaTextPath()) + link("html", a.javaHtmlPath()) + "</div>";
    }

    private String link(String label, String path) {
        if (path == null || path.isBlank()) {
            return "<span class=\"link-missing\" title=\"not produced\">" + esc(label) + "</span>";
        }
        String href = linkBase.isEmpty() ? path : linkBase + "/" + path;
        return "<a class=\"link\" href=\"" + esc(href) + "\" title=\"" + esc(path) + "\">" + esc(label) + "</a>";
    }

    private static Verdict verdict(ParitySummary.Totals t) {
        if (t.total() == 0) {
            return new Verdict("NO DATA", "grey", "No accounts in this parity run");
        }
        if (t.diff() == 0 && t.missing() == 0) {
            return new Verdict("FULL PARITY", "match",
                    t.match() + " of " + t.total() + " accounts match");
        }
        String detail = t.diff() + " diff, " + t.missing() + " missing of " + t.total() + " accounts";
        return new Verdict("PARITY GAPS", t.diff() > 0 ? "diff" : "missing", detail);
    }

    static String currency(BigDecimal amount) {
        BigDecimal scaled = amount.setScale(2, RoundingMode.HALF_UP);
        DecimalFormat fmt = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        if (scaled.signum() < 0) {
            return "-$" + fmt.format(scaled.negate());
        }
        return "$" + fmt.format(scaled);
    }

    private static String orDash(String value) {
        return value == null || value.isBlank() ? "\u2014" : value;
    }

    static String esc(String raw) {
        StringBuilder sb = new StringBuilder(raw.length() + 16);
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    private record Verdict(String text, String cssClass, String detail) {
    }

    private String css() {
        return """
                :root {
                  --bg: #f4f6f8;
                  --panel: #ffffff;
                  --ink: #1b2733;
                  --ink-soft: #5b6b7b;
                  --line: #dde3ea;
                  --match: #1f8a4c;
                  --match-bg: #e6f5ec;
                  --diff: #b4531a;
                  --diff-bg: #fdefe3;
                  --missing: #6b7785;
                  --missing-bg: #eceff2;
                  --accent: #1c3f6e;
                }
                * { box-sizing: border-box; }
                body {
                  margin: 0;
                  padding: 32px 28px 48px;
                  background: var(--bg);
                  color: var(--ink);
                  font: 14px/1.45 -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                }
                h1 { font-size: 22px; margin: 0 0 12px; letter-spacing: .2px; }
                h2 { font-size: 15px; margin: 0 0 12px; text-transform: uppercase; letter-spacing: .8px; color: var(--ink-soft); }
                .head {
                  display: flex; flex-wrap: wrap; gap: 20px; justify-content: space-between; align-items: stretch;
                  background: var(--panel); border: 1px solid var(--line); border-radius: 10px;
                  padding: 22px 24px; margin-bottom: 18px;
                }
                .head-main { min-width: 280px; }
                .meta { display: grid; grid-template-columns: auto auto; gap: 4px 16px; margin: 0; }
                .meta dt { color: var(--ink-soft); font-size: 12px; text-transform: uppercase; letter-spacing: .6px; }
                .meta dd { margin: 0; font-weight: 600; font-variant-numeric: tabular-nums; }
                .verdict {
                  display: flex; flex-direction: column; justify-content: center; gap: 2px;
                  min-width: 240px; padding: 16px 20px; border-radius: 8px; border: 1px solid transparent;
                }
                .verdict-label { font-size: 11px; text-transform: uppercase; letter-spacing: .8px; opacity: .8; }
                .verdict-value { font-size: 22px; letter-spacing: .5px; }
                .verdict-detail { font-size: 12px; opacity: .85; }
                .verdict-match { background: var(--match-bg); border-color: var(--match); color: var(--match); }
                .verdict-diff { background: var(--diff-bg); border-color: var(--diff); color: var(--diff); }
                .verdict-missing, .verdict-grey { background: var(--missing-bg); border-color: var(--missing); color: var(--missing); }
                .cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); gap: 14px; margin-bottom: 22px; }
                .card {
                  background: var(--panel); border: 1px solid var(--line); border-left-width: 5px;
                  border-radius: 8px; padding: 14px 16px; display: flex; flex-direction: column; gap: 6px;
                }
                .card-label { font-size: 12px; text-transform: uppercase; letter-spacing: .7px; color: var(--ink-soft); }
                .card-value { font-size: 28px; font-weight: 700; font-variant-numeric: tabular-nums; }
                .card-total { border-left-color: var(--accent); }
                .card-match { border-left-color: var(--match); }
                .card-match .card-value { color: var(--match); }
                .card-diff { border-left-color: var(--diff); }
                .card-diff .card-value { color: var(--diff); }
                .card-missing { border-left-color: var(--missing); }
                .card-missing .card-value { color: var(--missing); }
                .table-wrap { background: var(--panel); border: 1px solid var(--line); border-radius: 10px; padding: 20px 22px; }
                table.accounts { width: 100%; border-collapse: collapse; }
                table.accounts th, table.accounts td {
                  text-align: left; padding: 10px 12px; border-bottom: 1px solid var(--line); vertical-align: top;
                }
                table.accounts thead th {
                  position: sticky; top: 0; background: var(--panel); z-index: 1;
                  font-size: 11px; text-transform: uppercase; letter-spacing: .7px; color: var(--ink-soft);
                  border-bottom: 2px solid var(--line);
                }
                table.accounts tbody tr:nth-child(even) { background: #fafbfc; }
                .num { text-align: right; font-variant-numeric: tabular-nums; }
                .acct { font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace; white-space: nowrap; }
                .amount { font-weight: 600; white-space: nowrap; }
                .amount.negative { color: #b3261e; }
                .badge {
                  display: inline-block; padding: 3px 10px; border-radius: 999px;
                  font-size: 11px; font-weight: 700; letter-spacing: .7px; border: 1px solid transparent;
                }
                .badge-match { background: var(--match-bg); color: var(--match); border-color: var(--match); }
                .badge-diff { background: var(--diff-bg); color: var(--diff); border-color: var(--diff); }
                .badge-missing { background: var(--missing-bg); color: var(--missing); border-color: var(--missing); }
                .row-diff { background: #fffaf5 !important; }
                .row-missing { background: #f7f8fa !important; }
                .diff-line { display: flex; gap: 8px; justify-content: space-between; max-width: 220px; }
                .diff-key { color: var(--ink-soft); font-size: 12px; }
                .diff-val { font-weight: 700; font-variant-numeric: tabular-nums; }
                .diff-note { margin-top: 4px; font-size: 12px; color: var(--missing); }
                .muted { color: var(--ink-soft); }
                .link-group { display: flex; align-items: baseline; gap: 8px; margin-bottom: 4px; white-space: nowrap; }
                .link-group-label { font-size: 11px; text-transform: uppercase; letter-spacing: .6px; color: var(--ink-soft); width: 44px; }
                a.link { color: var(--accent); text-decoration: none; border-bottom: 1px dotted var(--accent); font-size: 12px; }
                a.link:hover { border-bottom-style: solid; }
                .link-missing { color: #a4aeb9; font-size: 12px; text-decoration: line-through; }
                .empty { color: var(--ink-soft); font-style: italic; margin: 0; }
                .foot { margin-top: 18px; font-size: 12px; color: var(--ink-soft); text-align: center; }
                """;
    }
}
