import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { vi, describe, expect, it, beforeEach } from "vitest";
import App from "./App";

function response<T>(data: T, message = "", nextRoute = "") {
  return { data, message, errorField: "", nextRoute, context: { fromProgram: "COMEN01C", userId: "USER001", userType: "U" } };
}

beforeEach(() => {
  sessionStorage.clear();
  vi.restoreAllMocks();
});

describe("CardDemo SPA", () => {
  it("renders the exact server signon failure message", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(new Response(JSON.stringify(response(null, "Wrong Password. Try again ...")), { status: 200 }));
    render(<MemoryRouter initialEntries={["/signon"]}><App /></MemoryRouter>);
    fireEvent.change(screen.getByLabelText("User ID"), { target: { value: "USER001" } });
    fireEvent.change(screen.getByLabelText("Password"), { target: { value: "BAD" } });
    fireEvent.click(screen.getByRole("button", { name: /enter/i }));
    expect(await screen.findByText("Wrong Password. Try again ...")).toBeInTheDocument();
  });

  it("navigates to the main menu after signon success", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(new Response(JSON.stringify(response({ token: "header.payload.signature", userId: "USER001", role: "ROLE_USER" })), { status: 200 }));
    render(<MemoryRouter initialEntries={["/signon"]}><App /></MemoryRouter>);
    fireEvent.change(screen.getByLabelText("User ID"), { target: { value: "USER001" } });
    fireEvent.change(screen.getByLabelText("Password"), { target: { value: "PASS" } });
    fireEvent.click(screen.getByRole("button", { name: /enter/i }));
    await waitFor(() => expect(screen.getByText("CARDDEMO MAIN MENU")).toBeInTheDocument());
  });

  it("renders the menu options returned for a user role", async () => {
    sessionStorage.setItem("carddemo.jwt", "token");
    vi.spyOn(globalThis, "fetch").mockResolvedValue(new Response(JSON.stringify(response({ options: [{ number: 1, label: "Account View", program: "COACTVWC", userType: "U" }, { number: 2, label: "Admin", program: "COUSR00C", userType: "A" }] })), { status: 200 }));
    render(<MemoryRouter initialEntries={["/menu"]}><App /></MemoryRouter>);
    expect(await screen.findByText("Account View")).toBeInTheDocument();
    expect(screen.queryByText("Admin")).not.toBeInTheDocument();
  });

  it("shows the add-transaction required-field validation", async () => {
    sessionStorage.setItem("carddemo.jwt", "token");
    render(<MemoryRouter initialEntries={["/transactions/add"]}><App /></MemoryRouter>);
    fireEvent.click(screen.getByRole("button", { name: /enter/i }));
    expect(await screen.findByText("Account or Card Number must be entered...")).toBeInTheDocument();
  });

  it("submits an account update only after the confirmation flow", async () => {
    sessionStorage.setItem("carddemo.jwt", "token");
    const fetchMock = vi.spyOn(globalThis, "fetch")
      .mockResolvedValueOnce(new Response(JSON.stringify(response({ accountId: 1, activeStatus: "Y", firstName: "Ada" })), { status: 200 }))
      .mockResolvedValueOnce(new Response(JSON.stringify(response({ accountId: 1 })), { status: 200 }));
    render(<MemoryRouter initialEntries={["/accounts/update"]}><App /></MemoryRouter>);
    fireEvent.change(screen.getByLabelText("Account ID"), { target: { value: "1" } });
    fireEvent.click(screen.getByRole("button", { name: /enter/i }));
    await waitFor(() => expect(screen.getByLabelText("First Name")).toHaveValue("Ada"));
    fireEvent.change(screen.getByLabelText("Confirm"), { target: { value: "Y" } });
    fireEvent.click(screen.getByRole("button", { name: /enter/i }));
    await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(2));
    expect(JSON.parse(String(fetchMock.mock.calls[1][1]?.body))).toMatchObject({ confirm: true });
  });

  it("shows page controls on the card list", async () => {
    sessionStorage.setItem("carddemo.jwt", "token");
    vi.spyOn(globalThis, "fetch").mockResolvedValue(new Response(JSON.stringify(response({ cards: [], page: 0, pageSize: 7, nextPage: true, previousPage: false })), { status: 200 }));
    render(<MemoryRouter initialEntries={["/cards"]}><App /></MemoryRouter>);
    await waitFor(() => expect(screen.getByRole("button", { name: "PF8 Page Down" })).toBeInTheDocument());
    expect(screen.getByRole("button", { name: /PF7 Page Up/ })).toBeInTheDocument();
  });

  it("uses PF3 to return to the caller route", async () => {
    sessionStorage.setItem("carddemo.jwt", "token");
    vi.spyOn(globalThis, "fetch").mockResolvedValue(new Response(JSON.stringify(response({ options: [] })), { status: 200 }));
    render(<MemoryRouter initialEntries={["/transactions/add"]}><App /></MemoryRouter>);
    fireEvent.keyDown(window, { key: "F3" });
    await waitFor(() => expect(screen.getByText("CARDDEMO MAIN MENU")).toBeInTheDocument());
  });
});
