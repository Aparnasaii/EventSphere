/* EventSphere Architecture & Dependency Guide — DOCX generator */
process.env.NODE_PATH = "C:\\Users\\2485781\\AppData\\Roaming\\npm\\node_modules";
require("module").Module._initPaths();

const fs = require("fs");
const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  AlignmentType, LevelFormat, HeadingLevel, BorderStyle, WidthType,
  ShadingType, PageBreak, PageNumber, Header, Footer
} = require("docx");

/* ─────────────────────────── helpers ─────────────────────────── */
const COL = {
  brand:   "1E3A8A",   // deep indigo title
  accent:  "4338CA",   // brand-mid
  light:   "EEF2FF",   // pale indigo cell
  legendModule:   "6366F1",
  legendRoute:    "A78BFA",
  legendComp:     "10B981",
  legendShared:   "0D9488",
  legendService:  "F59E0B",
  legendGuard:    "EF4444",
  legendInterc:   "B91C1C",
  legendBackend:  "0EA5E9",
  border:  "C7D2FE",
  rowAlt:  "F5F3FF",
};

const border = { style: BorderStyle.SINGLE, size: 4, color: COL.border };
const borders = { top: border, bottom: border, left: border, right: border };
const cellMargin = { top: 100, bottom: 100, left: 140, right: 140 };

// Helper: Heading
const H = (text, level = HeadingLevel.HEADING_1) =>
  new Paragraph({ heading: level, children: [new TextRun({ text, bold: true, color: COL.brand })] });

// Helper: small subhead
const SubH = (text) =>
  new Paragraph({
    spacing: { before: 200, after: 80 },
    children: [new TextRun({ text, bold: true, size: 24, color: COL.accent })],
  });

// Helper: normal paragraph
const P = (text, opts = {}) =>
  new Paragraph({
    spacing: { after: 100 },
    children: [new TextRun({ text, size: 22, ...opts })],
  });

// Helper: italic caption under a diagram
const Caption = (text) =>
  new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { before: 80, after: 200 },
    children: [new TextRun({ text, size: 20, italics: true, color: "6B7280" })],
  });

// Helper: monospace box (ASCII art / code)
const Mono = (text) =>
  new Paragraph({
    spacing: { after: 80 },
    children: [new TextRun({ text, font: "Consolas", size: 20 })],
  });

// Helper: bullet item
const Bullet = (text, opts = {}) =>
  new Paragraph({
    numbering: { reference: "bullets", level: 0 },
    children: [new TextRun({ text, size: 22, ...opts })],
  });

// Bullet with inline bold lead
const BulletKV = (key, val) =>
  new Paragraph({
    numbering: { reference: "bullets", level: 0 },
    children: [
      new TextRun({ text: key, bold: true, size: 22 }),
      new TextRun({ text: " — " + val, size: 22 }),
    ],
  });

// Helper: spacer
const Spacer = () => new Paragraph({ children: [new TextRun(" ")] });
const PB = () => new Paragraph({ children: [new PageBreak()] });

// Helper: colored legend swatch cell
const swatchCell = (color, label) =>
  new TableCell({
    borders,
    width: { size: 9360, type: WidthType.DXA },
    margins: cellMargin,
    children: [
      new Paragraph({
        children: [
          new TextRun({ text: "  ", shading: { fill: color, type: ShadingType.CLEAR } }),
          new TextRun({ text: "    " + label, size: 22 }),
        ],
      }),
    ],
  });

// Helper: colored box paragraph (simulates diagram node) — single cell table
const boxRow = (fill, title, sub) =>
  new Table({
    width: { size: 7000, type: WidthType.DXA },
    columnWidths: [7000],
    alignment: AlignmentType.CENTER,
    rows: [
      new TableRow({
        children: [
          new TableCell({
            borders,
            width: { size: 7000, type: WidthType.DXA },
            margins: { top: 140, bottom: 140, left: 200, right: 200 },
            shading: { fill, type: ShadingType.CLEAR },
            children: [
              new Paragraph({
                alignment: AlignmentType.CENTER,
                children: [new TextRun({ text: title, bold: true, size: 24, color: "FFFFFF" })],
              }),
              ...(sub
                ? [new Paragraph({
                    alignment: AlignmentType.CENTER,
                    children: [new TextRun({ text: sub, size: 20, color: "FFFFFF" })],
                  })]
                : []),
            ],
          }),
        ],
      }),
    ],
  });

const arrow = () =>
  new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { before: 40, after: 40 },
    children: [new TextRun({ text: "▼", size: 28, color: COL.accent, bold: true })],
  });

/* ───────────────────────── table builder ──────────────────────── */
function makeTable(headers, rows, widths) {
  const total = widths.reduce((a, b) => a + b, 0);
  const headerRow = new TableRow({
    tableHeader: true,
    children: headers.map((h, i) =>
      new TableCell({
        borders,
        width: { size: widths[i], type: WidthType.DXA },
        margins: cellMargin,
        shading: { fill: COL.brand, type: ShadingType.CLEAR },
        children: [
          new Paragraph({
            children: [new TextRun({ text: h, bold: true, color: "FFFFFF", size: 22 })],
          }),
        ],
      })
    ),
  });
  const bodyRows = rows.map((r, idx) =>
    new TableRow({
      children: r.map((c, i) =>
        new TableCell({
          borders,
          width: { size: widths[i], type: WidthType.DXA },
          margins: cellMargin,
          shading: { fill: idx % 2 ? COL.rowAlt : "FFFFFF", type: ShadingType.CLEAR },
          children: [new Paragraph({ children: [new TextRun({ text: c, size: 22 })] })],
        })
      ),
    })
  );
  return new Table({
    width: { size: total, type: WidthType.DXA },
    columnWidths: widths,
    rows: [headerRow, ...bodyRows],
  });
}

/* ═══════════════════════════════════════════════════════════════
   DOCUMENT CONTENT
   ═══════════════════════════════════════════════════════════════ */
const content = [];

/* ── COVER ── */
content.push(
  new Paragraph({ spacing: { before: 1800 } }),
  new Paragraph({
    alignment: AlignmentType.LEFT,
    spacing: { after: 120 },
    children: [new TextRun({ text: "EventSphere Frontend", bold: true, size: 80, color: COL.brand })],
  }),
  new Paragraph({
    alignment: AlignmentType.LEFT,
    spacing: { after: 60 },
    children: [new TextRun({ text: "Architecture & Dependency Guide", size: 44, color: "1F2937" })],
  }),
  new Paragraph({
    alignment: AlignmentType.LEFT,
    spacing: { after: 600 },
    children: [new TextRun({ text: "A beginner-friendly visual tour of every file and how they connect", italics: true, size: 26, color: "6B7280" })],
  }),
  new Paragraph({
    spacing: { after: 120 },
    children: [new TextRun({ text: "Stack: Angular 16 • RxJS • Angular Router • HttpClient", size: 24 })],
  }),
  new Paragraph({
    spacing: { after: 600 },
    children: [new TextRun({ text: "Backend gateway: http://localhost:8081", size: 24 })],
  }),
);

// Inside table-of-contents card
content.push(
  new Table({
    width: { size: 9360, type: WidthType.DXA },
    columnWidths: [9360],
    rows: [
      new TableRow({
        children: [
          new TableCell({
            borders: {
              top: { style: BorderStyle.SINGLE, size: 6, color: COL.accent },
              bottom: { style: BorderStyle.SINGLE, size: 6, color: COL.accent },
              left: { style: BorderStyle.SINGLE, size: 6, color: COL.accent },
              right: { style: BorderStyle.SINGLE, size: 6, color: COL.accent },
            },
            margins: { top: 240, bottom: 240, left: 360, right: 360 },
            width: { size: 9360, type: WidthType.DXA },
            shading: { fill: COL.light, type: ShadingType.CLEAR },
            children: [
              new Paragraph({
                spacing: { after: 160 },
                children: [new TextRun({ text: "What is inside this guide", bold: true, color: COL.brand, size: 28 })],
              }),
              ...[
                "1.  How to read the diagrams (legend)",
                "2.  Bird's-eye view of the entire application",
                "3.  Bootstrap flow — how the app starts up",
                "4.  AppModule composition — what is declared / imported / provided",
                "5.  Routing map — every URL, its component, and which guard protects it",
                "6.  Service dependency graph — how services lean on each other",
                "7.  Guards & HTTP interceptors — the security layer",
                "8.  Feature: Auth (Login / Register / Forgot / Reset)",
                "9.  Feature: Events (List / Detail / Form)",
                "10. Feature: Tickets (My Tickets / Booking)",
                "11. Feature: Feedback (Rate event + reviews)",
                "12. Feature: Admin (Manage Users / Pending Organizers)",
                "13. Feature: Dashboard & Shared Navbar",
                "14. Backend API endpoint map",
                "15. Glossary — every Angular term you will see, explained",
              ].map(t =>
                new Paragraph({
                  spacing: { after: 40 },
                  children: [new TextRun({ text: t, size: 22 })],
                })
              ),
            ],
          }),
        ],
      }),
    ],
  })
);

content.push(
  Spacer(),
  new Paragraph({
    alignment: AlignmentType.CENTER,
    children: [new TextRun({
      text: "Tip: arrows in every diagram read as 'depends on' / 'calls' — start from the component box and follow the arrow to see what it uses.",
      italics: true, size: 20, color: "6B7280",
    })],
  }),
  PB()
);

/* ── 1. HOW TO READ DIAGRAMS ── */
content.push(
  H("1.  How to read these diagrams"),
  P("The whole guide uses one consistent visual language. Each colored rectangle is a file in the project; each arrow is a real relationship in the code. Once you know what the shapes and lines mean, every diagram tells you a story about the codebase."),
  SubH("Box colors (file types)"),
  new Table({
    width: { size: 9360, type: WidthType.DXA },
    columnWidths: [9360],
    rows: [
      new TableRow({ children: [swatchCell(COL.legendModule,  "NgModule — registers components & wiring (e.g. AppModule)")] }),
      new TableRow({ children: [swatchCell(COL.legendRoute,   "Route definition — a URL that maps to a component (RouterModule)")] }),
      new TableRow({ children: [swatchCell(COL.legendComp,    "Component — a screen / piece of UI (.component.ts)")] }),
      new TableRow({ children: [swatchCell(COL.legendShared,  "Shared component — reused across screens (e.g. NavbarComponent)")] }),
      new TableRow({ children: [swatchCell(COL.legendService, "Service — shared logic / data store (@Injectable)")] }),
      new TableRow({ children: [swatchCell(COL.legendGuard,   "Route guard — decides if you may enter a route")] }),
      new TableRow({ children: [swatchCell(COL.legendInterc,  "HTTP interceptor — sits in front of every network request")] }),
      new TableRow({ children: [swatchCell(COL.legendBackend, "Backend endpoint — the REST URL the service calls")] }),
    ],
  }),
  SubH("Arrow meanings"),
  Bullet("→  'uses' — the source file injects / calls the target"),
  Bullet("→  'declares / imports' — module-level wiring"),
  Bullet("→  'navigates to' — Router.navigate or routerLink to a route"),
  Bullet("→  'HTTP call' — sends a request to a backend endpoint"),
  Bullet("- - →  'guarded by' — dashed = optional / conditional path"),
  SubH("Where files live (folder layout)"),
  Mono("Frontend/src/app/"),
  Mono(" ├── app.module.ts            ← The one NgModule that wires everything"),
  Mono(" ├── app-routing.module.ts    ← All URL → component mappings"),
  Mono(" ├── app.component.*          ← Root shell (hosts <router-outlet>)"),
  Mono(" ├── guards/                  ← AuthGuard, RoleGuard"),
  Mono(" ├── interceptors/            ← JwtInterceptor, ErrorInterceptor"),
  Mono(" ├── models/                  ← TypeScript interfaces (User, Event, Ticket, Feedback)"),
  Mono(" ├── services/                ← Singletons (auth, user, event, ticket, feedback, notification)"),
  Mono(" └── components/              ← Feature screens"),
  Mono("      ├── auth/               (login, register, forgot-password, reset-password)"),
  Mono("      ├── dashboard/"),
  Mono("      ├── events/             (event-list, event-detail, event-form)"),
  Mono("      ├── tickets/my-tickets/"),
  Mono("      ├── feedback/"),
  Mono("      ├── admin/              (manage-users, pending-organizers)"),
  Mono("      └── shared/navbar/"),
  PB()
);

/* ── 2. BIRD'S-EYE VIEW ── */
content.push(
  H("2.  Bird's-eye view of the app"),
  P("This is the simplest possible mental model. From browser to backend, every request flows through these layers. Each later diagram zooms into one of these boxes."),
  Spacer(),
  boxRow("4B5563", "Browser", "Loads index.html, runs main.ts"),
  arrow(),
  boxRow(COL.legendModule, "AppModule + AppComponent", "Bootstrap point — registers everything, mounts root"),
  arrow(),
  boxRow(COL.legendRoute, "Router (app-routing.module.ts)", "Decides which component renders for the URL"),
  arrow(),
  boxRow(COL.legendComp, "Feature Components", "auth / events / tickets / feedback / admin / dashboard"),
  arrow(),
  boxRow(COL.legendService, "Core Services + HttpClient", "Holds state, builds REST requests"),
  arrow(),
  boxRow(COL.legendInterc, "JwtInterceptor + ErrorInterceptor", "Adds JWT to every request, handles 401/403/500"),
  arrow(),
  boxRow(COL.legendBackend, "API Gateway (Spring Cloud)", "http://localhost:8081"),
  Caption("Read top-to-bottom: a user click ends up as an HTTP request to the API gateway."),
  SubH("Take-aways for a beginner"),
  Bullet("Every Angular app has one root NgModule (here: AppModule) that wires everything together."),
  Bullet("The Router replaces the content inside <router-outlet> with whatever Component the URL maps to."),
  Bullet("Components do not talk to the backend directly — they ask injected Services to do it."),
  Bullet("The JwtInterceptor secretly attaches the JWT to every HTTP request, so services don't have to."),
  Bullet("The ErrorInterceptor centralises 401 handling — when the token expires anywhere, the user is bounced to /login."),
  PB()
);

/* ── 3. BOOTSTRAP FLOW ── */
content.push(
  H("3.  Bootstrap flow — how the app starts"),
  P("When you open the site in a browser, this is the chain of events. main.ts is the very first line of your code that runs."),
  Spacer(),
  boxRow("4B5563", "main.ts", "Entry file"),
  arrow(),
  boxRow("4B5563", "platformBrowserDynamic()", "Angular runtime"),
  arrow(),
  boxRow(COL.legendModule, "AppModule", "app.module.ts — @NgModule metadata"),
  arrow(),
  boxRow(COL.legendComp, "AppComponent", "Root shell — template hosts <router-outlet>"),
  arrow(),
  boxRow(COL.legendRoute, "AppRoutingModule", "Loaded via imports[] — registers all routes"),
  Caption("main.ts boots AppModule. AppModule registers everything, provides the two interceptors, and mounts AppComponent."),
  SubH("Concretely, in code"),
  Mono("// main.ts"),
  Mono("platformBrowserDynamic().bootstrapModule(AppModule)"),
  P("That single line is the whole 'go' button. Everything else cascades from AppModule's @NgModule decorator metadata — declarations, imports, providers, and bootstrap."),
  SubH("What gets created during bootstrap"),
  Bullet("Angular's dependency injection container is created."),
  Bullet("Every service marked providedIn: 'root' is registered for lazy instantiation on first injection."),
  Bullet("Both interceptors (JwtInterceptor + ErrorInterceptor) are wired into HttpClient's request pipeline."),
  Bullet("AppComponent's template renders, which contains <router-outlet>."),
  Bullet("Router reads the current URL and matches it against the routes table — the matched component is created and slotted in."),
  PB()
);

/* ── 4. APPMODULE COMPOSITION ── */
content.push(
  H("4.  AppModule composition"),
  P("AppModule is a single class with the @NgModule decorator. The decorator has four slots: declarations (components owned by this module), imports (other modules this module depends on), providers (singletons available app-wide), and bootstrap (which component to render first)."),
  Spacer(),
  makeTable(
    ["Slot", "Contents"],
    [
      ["declarations (14)",
       "AppComponent, NavbarComponent, LoginComponent, RegisterComponent, ForgotPasswordComponent, ResetPasswordComponent, DashboardComponent, EventListComponent, EventDetailComponent, EventFormComponent, ManageUsersComponent, PendingOrganizersComponent, MyTicketsComponent, FeedbackComponent"],
      ["imports",
       "BrowserModule, AppRoutingModule, ReactiveFormsModule, FormsModule, HttpClientModule, LucideAngularModule.pick({ ...icons })"],
      ["providers",
       "{ provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },  { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true }"],
      ["bootstrap", "AppComponent"],
    ],
    [2400, 6960]
  ),
  Caption("AppModule is one big 'phone book': it lists every component the app owns, every framework module it leans on, and registers both interceptors so every HTTP request gets a JWT and gracefully handles errors."),
  SubH("Why both interceptors are 'multi: true'"),
  P("HTTP_INTERCEPTORS is a multi-provider token: Angular collects ALL classes registered under it and runs them in order. JwtInterceptor runs first (adds the token), ErrorInterceptor runs second (catches the response). Setting multi: true tells Angular not to overwrite the previous registration."),
  SubH("Icon registration"),
  P("LucideAngularModule.pick({ Ticket, Star, ... }) is a tree-shaking trick: only the icons you list are bundled. Unlisted icons would silently render blank if you use them in templates — a real bug we hit with pause-circle and play-circle in the admin screen."),
  PB()
);

/* ── 5. ROUTING MAP ── */
content.push(
  H("5.  Routing map"),
  P("AppRoutingModule contains an array of route objects. Each object maps a URL path to a component, and may also list one or more guards in canActivate. When a guard returns false the navigation is cancelled."),
  Spacer(),
  makeTable(
    ["Public (no guard)", "AuthGuard (logged in)", "RoleGuard (role-restricted)"],
    [
      ["/login\nLoginComponent", "/dashboard\nDashboardComponent", "/events/new\nEventFormComponent\nROLE_ORGANIZER, ROLE_ADMIN"],
      ["/register\nRegisterComponent", "/tickets\nMyTicketsComponent", "/events/edit/:id\nEventFormComponent\nROLE_ORGANIZER, ROLE_ADMIN"],
      ["/forgot-password\nForgotPasswordComponent", "/feedback/:eventId\nFeedbackComponent", "/admin/users\nManageUsersComponent\nROLE_ADMIN"],
      ["/reset-password\nResetPasswordComponent", "/events\nEventListComponent", "/admin/pending\nPendingOrganizersComponent\nROLE_ADMIN"],
      ["**  →  redirect to /login", "/events/:id\nEventDetailComponent", ""],
    ],
    [3120, 3120, 3120]
  ),
  Caption("Public routes need no token; AuthGuard routes need a valid JWT; RoleGuard routes need a JWT whose role claim is in the allowed list."),
  SubH("How a route resolves at runtime"),
  Bullet("User clicks a routerLink — Router compares the URL to the route list top to bottom."),
  Bullet("If the matched route has canActivate guards, each guard runs in order."),
  Bullet("AuthGuard checks localStorage for the JWT — if missing, redirects to /login."),
  Bullet("RoleGuard decodes the JWT, compares its role claim to data.roles — if mismatch, redirects to /dashboard."),
  Bullet("If everything passes, the matched component is created and mounted into <router-outlet>."),
  Bullet("The '**' wildcard at the bottom is the catch-all: any unknown URL bounces back to /login."),
  SubH("Query parameters drive UI mode (not new routes)"),
  P("The Event List component supports five distinct modes through query params instead of separate routes:"),
  Mono("/events                   → upcoming events for everyone"),
  Mono("/events?past=true         → past events the attendee booked"),
  Mono("/events?registered=true   → attendee's upcoming booked events"),
  Mono("/events?view=all          → organizer sees ALL events, not just theirs"),
  Mono("/events?category=Music    → filtered by category"),
  P("Because Angular re-uses the SAME component instance when only query params change, we subscribe to route.queryParamMap (Observable) instead of route.snapshot — otherwise the mode would not update on back/forward navigation."),
  PB()
);

/* ── 6. SERVICE DEPENDENCY GRAPH ── */
content.push(
  H("6.  Service dependency graph"),
  P("Services are @Injectable classes that hold reusable logic and state. They are created once (providedIn: 'root') and injected into anyone who asks. Most services only depend on Angular's HttpClient, but a few lean on each other."),
  Spacer(),
  makeTable(
    ["Service", "Depends on", "What it owns"],
    [
      ["AuthService", "HttpClient, JwtHelper", "Login state. Stores JWT in localStorage. Exposes getCurrentUser() / isAdmin() / isOrganizer() / isAttendee()."],
      ["UserService", "HttpClient", "Admin user management — getAllUsers, approveOrganizer, suspendUser, reactivateUser, deleteUser."],
      ["EventService", "HttpClient", "CRUD on events, search, organizer's events, triggerFeedback(id)."],
      ["TicketService", "HttpClient", "bookTicket, cancelTicket, getTicketsByUser, payment success handler."],
      ["FeedbackService", "HttpClient", "submitFeedback (PUT), initiateFeedback (POST), getUserFeedback, getAverageRating, getComments."],
      ["NotificationService", "HttpClient", "Optional client for the notification microservice — used for manual template triggers."],
    ],
    [2200, 2200, 4960]
  ),
  Caption("All services talk to the API gateway (port 8081). The dashboard uses several services in parallel via forkJoin."),
  SubH("Cross-service collaboration"),
  Bullet("DashboardComponent calls EventService + TicketService + FeedbackService in parallel via forkJoin to render its 'pending feedback' reminders."),
  Bullet("FeedbackComponent uses EventService + TicketService + FeedbackService together — checks the user has a CONFIRMED ticket AND the event has ended before showing the form."),
  Bullet("EventDetailComponent uses EventService + TicketService — fetches event details, then on Book Now passes to TicketService.bookTicket."),
  Bullet("AuthService is the ONLY service that reads localStorage directly — every other service treats authentication as 'someone else's problem' (handled by the JwtInterceptor)."),
  PB()
);

/* ── 7. GUARDS & INTERCEPTORS ── */
content.push(
  H("7.  Guards & the HTTP interceptors"),
  P("These four small files implement the entire security model on the frontend. Two of them sit in front of route navigations (guards), and two sit in front of every HTTP request (interceptors)."),
  SubH("Route navigation flow"),
  boxRow("9CA3AF", "User clicks routerLink", "Router starts navigating"),
  arrow(),
  boxRow(COL.legendGuard, "AuthGuard.canActivate()", "Has JWT in localStorage?"),
  arrow(),
  boxRow(COL.legendGuard, "RoleGuard.canActivate()", "JWT.role ∈ data.roles?"),
  arrow(),
  boxRow(COL.legendComp, "Component is created", "Rendered in <router-outlet>"),
  P("If AuthGuard fails  →  redirect to /login. If RoleGuard fails  →  redirect to /dashboard."),
  Spacer(),
  SubH("HTTP request flow"),
  boxRow(COL.legendComp, "Component calls service.method()", "e.g. ticketSvc.bookTicket(req)"),
  arrow(),
  boxRow("4B5563", "HttpClient builds request", "no Authorization header yet"),
  arrow(),
  boxRow(COL.legendInterc, "JwtInterceptor.intercept()", "Adds 'Authorization: Bearer <jwt>'"),
  arrow(),
  boxRow(COL.legendInterc, "ErrorInterceptor.intercept()", "catchError pipe: 401 → logout, 403/500 → toast"),
  arrow(),
  boxRow(COL.legendBackend, "API Gateway → microservice", "returns 2xx / 4xx / 5xx"),
  Caption("Left: guards run BEFORE a route activates, gating access. Right: interceptors run BEFORE every request leaves the browser, attaching the JWT and watching for errors."),
  SubH("Files involved"),
  Bullet("guards/auth.guard.ts — checks localStorage.getItem('token') is non-null. Protects /dashboard, /tickets, /feedback, /events/new, all admin routes."),
  Bullet("guards/role.guard.ts — decodes the JWT's role claim and compares to data.roles in the route definition. Protects /admin/* (ROLE_ADMIN) and /events/new + edit (ROLE_ORGANIZER, ROLE_ADMIN)."),
  Bullet("interceptors/jwt.interceptor.ts — registered once in AppModule providers; runs for every HttpClient call in the app."),
  Bullet("interceptors/error.interceptor.ts — uses RxJS catchError; on 401 calls auth.logout() and navigates to /login."),
  PB()
);

/* ── 8. FEATURE: AUTH ── */
content.push(
  H("8.  Feature group — Auth"),
  P("Four screens handle 'who are you'. LoginComponent authenticates against /users/api/auth/login and stores the returned JWT, RegisterComponent creates a new account, ForgotPasswordComponent emails a reset link, ResetPasswordComponent consumes the token."),
  Spacer(),
  makeTable(
    ["Route", "Component", "Service / dependency"],
    [
      ["/login\n(public)", "LoginComponent\ncomponents/auth/login", "AuthService, Router, FormsModule, ToastDisplay (inline)"],
      ["/register\n(public)", "RegisterComponent\ncomponents/auth/register", "AuthService, Router, ReactiveForms (FormBuilder, Validators)"],
      ["/forgot-password\n(public)", "ForgotPasswordComponent\ncomponents/auth/forgot-password", "AuthService.forgotPassword(email), Router"],
      ["/reset-password\n(public)", "ResetPasswordComponent\ncomponents/auth/reset-password", "AuthService.resetPassword(token, newPw), ActivatedRoute (reads ?token=)"],
    ],
    [2400, 2960, 4000]
  ),
  Caption("Auth screens collaborate with AuthService (state) and use HttpClient via the service layer."),
  SubH("What is happening here, in plain English"),
  Bullet("LoginComponent reads the role from the JWT after a successful login and routes the user to /dashboard. Suspended/inactive/rejected accounts get a friendly 403 message instead of a generic error."),
  Bullet("RegisterComponent — organizers get UserStatus = PENDING and cannot log in until an admin approves them. Attendees go straight to ACTIVE."),
  Bullet("ForgotPasswordComponent calls the backend, which generates a UUID token, stores it with a 1-hour expiry, and emails it via Gmail SMTP."),
  Bullet("ResetPasswordComponent reads the token from the URL (?token=...) and posts it with the new password — backend validates expiry, hashes the new password with BCrypt, and deletes the token."),
  SubH("JWT contents"),
  P("After login, the JWT payload contains five claims: sub (email), role, userId, iat (issued at), exp (expiry — 1 hour from issue). AuthService decodes them with a tiny base64 decoder — no external library."),
  PB()
);

/* ── 9. FEATURE: EVENTS ── */
content.push(
  H("9.  Feature group — Events"),
  P("The core of the app. EventListComponent is the most complex frontend screen — it supports five viewing modes via query params. EventDetailComponent shows a single event with a booking modal. EventFormComponent is a reactive form for create/edit (organizer / admin only)."),
  Spacer(),
  makeTable(
    ["Route", "Component", "Service / dependency"],
    [
      ["/events\n(public)\n?past, ?registered, ?view, ?category", "EventListComponent\ncomponents/events/event-list", "EventService, TicketService (for registered/past mode), AuthService, ActivatedRoute (queryParamMap)"],
      ["/events/:id\n(public)", "EventDetailComponent\ncomponents/events/event-detail", "EventService, TicketService, AuthService"],
      ["/events/new\n[ROLE_ORGANIZER, ROLE_ADMIN]", "EventFormComponent\ncomponents/events/event-form", "EventService, AuthService, FormBuilder, Validators, ActivatedRoute (for edit)"],
      ["/events/edit/:id\n[ROLE_ORGANIZER, ROLE_ADMIN]", "EventFormComponent (reused)", "Same as above; loads existing event into the form on init"],
    ],
    [3200, 2960, 3200]
  ),
  Caption("Events components reach into TicketService and AuthService to drive role-aware UI."),
  SubH("What is happening here, in plain English"),
  Bullet("EventListComponent subscribes to ActivatedRoute.queryParamMap (NOT snapshot) so that switching between modes via the same path actually triggers a reload. Without this, going /events?past=true → /events?past=false would show stale data."),
  Bullet("'Registered mode' uses forkJoin to load all events + user's tickets in parallel, filters tickets to status === CONFIRMED or PENDING_PAYMENT (excludes cancelled), then intersects with events."),
  Bullet("EventDetailComponent renders the booking modal with a quantity stepper. On confirm, it calls ticketSvc.bookTicket — if the event is paid, the response includes a checkoutUrl and the browser is redirected to Stripe."),
  Bullet("EventFormComponent uses Reactive Forms with two custom validators: futureTimeValidator (startTime must be in the future) and endAfterStartValidator (endTime > startTime AND duration ≥ 1 hour)."),
  Bullet("On edit, the form patches existing values; on save, the same payload is sent via PUT instead of POST."),
  PB()
);

/* ── 10. FEATURE: TICKETS ── */
content.push(
  H("10.  Feature group — Tickets"),
  P("Once a user has booked, MyTicketsComponent lists every ticket with status chips and actions. Cancel is allowed only before the event starts. 'Rate Event' appears on CONFIRMED tickets for events that have ended."),
  Spacer(),
  makeTable(
    ["Route", "Component", "Service / dependency"],
    [
      ["/tickets\n[AuthGuard]\n?payment=success / ?payment=failed",
       "MyTicketsComponent\ncomponents/tickets/my-tickets",
       "TicketService, EventService (for event name/time enrichment), AuthService, ActivatedRoute"],
    ],
    [3200, 3000, 3160]
  ),
  Caption("Stripe redirects back here with ?payment=success — the component reads it and shows a toast."),
  SubH("What is happening here, in plain English"),
  Bullet("On init, MyTicketsComponent calls ticketSvc.getTicketsByUser(userId) — gets an array of tickets, each with eventId."),
  Bullet("It then fires N parallel eventSvc.getEventById(t.eventId) calls via forkJoin (with catchError → null fallback) and merges name/startTime/endTime onto each ticket as a TicketView."),
  Bullet("isEventEnded(ticket) compares ticket.eventEndTime ?? ticket.eventStartTime against now() — controls whether the Cancel button shows or the Rate button shows."),
  Bullet("On Cancel, the component optimistically flips status to CANCELLED locally for instant UI feedback, then awaits the backend confirmation."),
  Bullet("Booking quantity is displayed via a small indigo chip — important detail because backend stores one row with quantity: N rather than N rows."),
  SubH("Stripe payment round-trip"),
  Mono("1. user clicks Book Now            (EventDetailComponent)"),
  Mono("2. POST /tickets/book              (TicketService)"),
  Mono("3. ticket saved as PENDING_PAYMENT (backend)"),
  Mono("4. backend creates Stripe Session  (Stripe API)"),
  Mono("5. response { ticketId, checkoutUrl }"),
  Mono("6. window.open(checkoutUrl)        (frontend)"),
  Mono("7. user pays on stripe.com"),
  Mono("8. Stripe redirects to             (browser)"),
  Mono("   /tickets/payment/success?session_id=cs_test_..."),
  Mono("9. ticket-service flips status to CONFIRMED"),
  Mono("10. 302 redirect to /tickets?payment=success"),
  Mono("11. MyTicketsComponent shows toast 'Payment successful!'"),
  PB()
);

/* ── 11. FEATURE: FEEDBACK ── */
content.push(
  H("11.  Feature group — Feedback"),
  P("FeedbackComponent is the most logic-heavy single screen. It loads in parallel: the event, the user's tickets, the existing feedback record, and the public stats. Based on these it picks ONE of six visual states to render."),
  Spacer(),
  makeTable(
    ["Route", "Component", "Service / dependency"],
    [
      ["/feedback/:eventId\n[AuthGuard]",
       "FeedbackComponent\ncomponents/feedback",
       "FeedbackService, EventService, TicketService, AuthService, ActivatedRoute"],
    ],
    [2800, 3000, 3560]
  ),
  Caption("Four services feed into one screen, but the UI never feels slow because everything loads in parallel via forkJoin."),
  SubH("Six possible visual states"),
  Bullet("1) Loading — spinner while pendingLoads > 0"),
  Bullet("2) No ticket — attendee hasn't booked this event"),
  Bullet("3) Event not ended — has ticket but startTime is in the future"),
  Bullet("4) Form not ready — event ended but no PENDING feedback record (then auto-trigger fires)"),
  Bullet("5) Form — feedbackId known; user can pick stars + write comment"),
  Bullet("6) Thank-you — submitted: animated 🎉 screen with their rating and comment"),
  SubH("Auto-initiate trick"),
  P("In the old design the organizer had to manually click 'Initiate Feedback' to create PENDING records. We removed that step: when an attendee opens the feedback page for an ended event with no PENDING record, the component silently calls feedbackSvc.initiateFeedback(eventId, name), waits 700 ms for the backend to commit the new rows, then re-fetches its own feedback record. The user sees the form appear automatically — they have no idea the trigger happened."),
  SubH("Admin override"),
  P("Admins do not have tickets — so they cannot submit feedback. The component detects auth.isAdmin() and renders an 'Admin Overview' panel instead of the form: average rating + review count, no input fields."),
  PB()
);

/* ── 12. FEATURE: ADMIN ── */
content.push(
  H("12.  Feature group — Admin"),
  P("Admin-only screens. RoleGuard requires ROLE_ADMIN. ManageUsersComponent is a full-featured table with stat cards as click-to-filter chips. PendingOrganizersComponent is a quick approve/reject queue."),
  Spacer(),
  makeTable(
    ["Route", "Component", "Service / dependency"],
    [
      ["/admin/users\n[RoleGuard: ROLE_ADMIN]",
       "ManageUsersComponent\ncomponents/admin/manage-users",
       "UserService (5 methods: getAllUsers, approveOrganizer, suspendUser, reactivateUser, deleteUser)"],
      ["/admin/pending\n[RoleGuard: ROLE_ADMIN]",
       "PendingOrganizersComponent\ncomponents/admin/pending-organizers",
       "UserService (getPendingOrganizers, approveOrganizer, rejectOrganizer)"],
    ],
    [3000, 3000, 3360]
  ),
  Caption("Two admin screens, one service — a clean fan-out pattern."),
  SubH("What is happening here, in plain English"),
  Bullet("ManageUsersComponent loads all users on init, then computes five stat counts via getters (totalCount, organizerCount, attendeeCount, pendingCount, suspendedCount)."),
  Bullet("Each stat card doubles as a filter — clicking 'Pending' sets activeFilter = 'pending' and the filtered getter narrows the table."),
  Bullet("Suspend / Reactivate / Delete each show a confirm() before firing, then patch the user in-place on success."),
  Bullet("Admin rows render with a red avatar gradient and a 'Protected' label — no destructive actions available (backend also enforces this with a 403)."),
  Bullet("If any action fails, handleActionError parses the error: 404 hints at 'restart the backend', 403 hints at 'permission', 0 hints at 'is the server running?'"),
  PB()
);

/* ── 13. DASHBOARD + NAVBAR ── */
content.push(
  H("13.  Feature group — Dashboard & Shared Navbar"),
  P("The Dashboard is a role-aware home page — it renders different content for attendees, organizers, and admins. The Navbar is the single shared component used on every authenticated screen."),
  Spacer(),
  makeTable(
    ["Route / Selector", "Component", "Service / dependency"],
    [
      ["/dashboard\n[AuthGuard]",
       "DashboardComponent\ncomponents/dashboard",
       "AuthService, EventService, TicketService, FeedbackService"],
      ["<app-navbar>\n(shared)",
       "NavbarComponent\ncomponents/shared/navbar",
       "AuthService, Router. Reused inside every protected page."],
    ],
    [3000, 3000, 3360]
  ),
  Caption("Dashboard fans out into 4 services; Navbar only needs AuthService for role-aware menu items."),
  SubH("What is happening here, in plain English"),
  Bullet("On init, the dashboard branches on auth.isOrganizer() / auth.isAttendee() / else admin — each branch fires a different forkJoin shape."),
  Bullet("For attendees: parallel calls to events.getAllEvents() + tickets.getTicketsByUser(userId). Then it builds upcomingEvents, pastEvents, myBookedEvents, todayEvents, and pendingFeedbackEvents."),
  Bullet("pendingFeedbackEvents uses feedback.getFeedbacksByUser(userId) (bulk) with a per-event fallback if that endpoint isn't available — events whose feedback status is not COMPLETED get an amber reminder card."),
  Bullet("For organizers: events.getAllEvents() + events.getEventsByOrganizer(userId). Today's event banner only fires when the organizer hosts something today."),
  Bullet("The Navbar reads auth.isAttendee() / isOrganizer() / isAdmin() to decide which links to render. It also exposes a dark-mode toggle that persists to localStorage."),
  PB()
);

/* ── 14. BACKEND API ENDPOINT MAP ── */
content.push(
  H("14.  Backend API endpoint map"),
  P("Every URL the frontend calls. The API gateway sits at http://localhost:8081 and dispatches to the right microservice. JwtInterceptor adds Authorization: Bearer <jwt> to every one of these requests."),
  Spacer(),
  makeTable(
    ["Service", "Method", "Endpoint", "Notes"],
    [
      ["AuthService", "POST", "/users/api/auth/login", "Returns { token, message }"],
      ["AuthService", "POST", "/users/api/auth/forgot-password", "Email reset link"],
      ["AuthService", "POST", "/users/api/auth/reset-password", "Consume token + set new password"],
      ["UserService", "POST", "/users/api/users/register", "New user (organizer goes PENDING)"],
      ["UserService", "GET", "/users/api/users/all", "Admin only"],
      ["UserService", "GET", "/users/api/users/{id}", "Public-ish; used by Feign too"],
      ["UserService", "GET", "/users/api/users/admin/pending", "Admin only"],
      ["UserService", "PUT", "/users/api/users/admin/approve/{id}", "Admin approves organizer"],
      ["UserService", "PUT", "/users/api/users/admin/reject/{id}", "Admin rejects organizer"],
      ["UserService", "PUT", "/users/api/users/admin/suspend/{id}", "Admin suspends user"],
      ["UserService", "PUT", "/users/api/users/admin/reactivate/{id}", "Admin reactivates user"],
      ["UserService", "DELETE", "/users/api/users/admin/{id}", "Admin deletes user"],
      ["EventService", "GET", "/events", "All events"],
      ["EventService", "GET", "/events/{id}", "Single event detail"],
      ["EventService", "POST", "/events", "Create — organizer/admin"],
      ["EventService", "PUT", "/events/{id}", "Update"],
      ["EventService", "DELETE", "/events/{id}", "Delete"],
      ["EventService", "GET", "/events/organizer/{organizerId}", "Organizer's own events"],
      ["EventService", "POST", "/events/{id}/feedback", "Initiate feedback cycle"],
      ["TicketService", "POST", "/tickets/book", "Returns checkoutUrl if paid"],
      ["TicketService", "GET", "/tickets/user/{userId}", "User's tickets"],
      ["TicketService", "PUT", "/tickets/cancel/{id}", "Cancel a ticket"],
      ["TicketService", "GET", "/tickets/payment/success?session_id=", "Stripe success webhook"],
      ["FeedbackService", "POST", "/feedbacks/feedback/initiate", "Create PENDING rows for all confirmed attendees"],
      ["FeedbackService", "PUT", "/feedbacks/feedback/submit/{id}", "Attendee submits rating"],
      ["FeedbackService", "GET", "/feedbacks/feedback/average/{eventId}", "Average rating"],
      ["FeedbackService", "GET", "/feedbacks/feedback/comments/{eventId}", "All public comments"],
      ["FeedbackService", "GET", "/feedbacks/feedback/event/{eId}/user/{uId}", "One user's record for one event"],
      ["FeedbackService", "GET", "/feedbacks/feedback/user/{userId}", "All a user's feedback (bulk for dashboard)"],
    ],
    [1900, 900, 3400, 3160]
  ),
  PB()
);

/* ── 15. GLOSSARY ── */
content.push(
  H("15.  Beginner glossary"),
  P("Every Angular term that appears in this guide, explained the way you wish someone had explained it to you on day one."),

  SubH("NgModule"),
  P("A class decorated with @NgModule. Think of it as a phone book: it lists which components, directives, and pipes belong together, which other modules they need, and what services to provide. This app has exactly one NgModule — AppModule."),

  SubH("Component"),
  P("A class decorated with @Component, paired with an HTML template and CSS file. Each component is one piece of the screen. The class is the brain, the template is the body."),

  SubH("Service"),
  P("A class decorated with @Injectable. Has no UI. Holds reusable logic (e.g. 'submit feedback') or shared state (e.g. 'who is logged in'). Created once when first injected (providedIn: 'root') and shared everywhere."),

  SubH("Dependency Injection"),
  P("Instead of doing new AuthService() you write constructor(private auth: AuthService). Angular sees the type and hands you the same shared instance. That is why services can be reused without manual plumbing."),

  SubH("Router & <router-outlet>"),
  P("The Router watches the URL and decides which Component to mount inside the <router-outlet> placeholder in your template. routerLink=\"/dashboard\" is how a template tells the Router 'go to /dashboard'."),

  SubH("Routes array"),
  P("A list of objects like { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] }. The Router picks the first matching path."),

  SubH("Route Guard (canActivate)"),
  P("A class implementing CanActivate. Returns true → the route loads. Returns false → the route is blocked and the guard usually redirects the user. EventSphere has two guards: AuthGuard (must have JWT) and RoleGuard (JWT role must match)."),

  SubH("HTTP Interceptor"),
  P("A class implementing HttpInterceptor. It runs for every HttpClient request, in order. EventSphere registers two: JwtInterceptor (adds Authorization header) and ErrorInterceptor (centralised 401/403/500 handling)."),

  SubH("HttpClient"),
  P("Angular's wrapper around fetch/XHR. Returns Observables instead of Promises so you can compose retries, transformations, and parallel calls via forkJoin."),

  SubH("Observable / RxJS"),
  P("A stream of values over time. You .subscribe() to receive them. Examples: route.queryParamMap pushes a new value every time the URL changes, so the EventList component can re-render."),

  SubH("forkJoin"),
  P("An RxJS operator that takes multiple Observables and emits ONCE when all of them have completed, with an array of their last values. EventSphere uses it heavily in the dashboard and feedback components to parallelise API calls — much faster than sequential .then() chains."),

  SubH("catchError"),
  P("An RxJS operator that intercepts errors in an Observable pipeline and lets you transform them or substitute a fallback value. We use it like .pipe(catchError(() => of([]))) so a failed sub-call doesn't kill the whole forkJoin."),

  SubH("BehaviorSubject"),
  P("A special Observable that remembers its latest value. Useful for shared state that new subscribers should see immediately. EventSphere doesn't use it heavily — most state is per-route — but it's the standard answer for 'how would you broadcast live updates between components'."),

  SubH("Reactive Forms (FormBuilder, FormGroup, Validators)"),
  P("A way to build forms in TypeScript instead of HTML attributes. EventFormComponent uses them — including two custom validators: futureTimeValidator (start > now) and endAfterStartValidator (end > start AND duration ≥ 1 h)."),

  SubH("Template-driven Forms ([(ngModel)])"),
  P("The simpler form pattern — two-way binding via ngModel. We use this for the login screen because it has only two fields. Reactive forms are preferred for anything more complex."),

  SubH("JWT (JSON Web Token)"),
  P("A signed string the backend issues on login. The frontend stores it in localStorage. JwtInterceptor attaches it as a Bearer token on each request so the gateway can identify the user. EventSphere uses HMAC-SHA256 with a 64-character secret and a 1-hour expiry."),

  SubH("localStorage"),
  P("A tiny key-value store the browser keeps between page reloads. This app uses one key: 'token' (set by AuthService) plus a 'theme' key for the dark-mode toggle."),

  SubH("Lazy-loaded vs eagerly-declared"),
  P("In this project every feature component is eagerly declared in AppModule. That is the simplest setup — everything ships in one bundle. Larger apps would split features into their own modules and lazy-load them via loadChildren in the routes."),

  SubH("queryParamMap vs paramMap"),
  P("paramMap is for path segments like /feedback/:eventId (read with route.snapshot.paramMap.get('eventId') because the path usually doesn't change while the component is alive). queryParamMap is for ?past=true style params and MUST be subscribed via the Observable because Angular re-uses the component instance when only the query string changes."),

  SubH("ActivatedRoute"),
  P("A service injected into a component that gives access to the URL params, query params, data attached to the route, and the URL fragment. Pair with Router (to programmatically navigate) and you have full URL control."),
);

/* ═══════════════════════════════════════════════════════════════
   ASSEMBLE DOCUMENT
   ═══════════════════════════════════════════════════════════════ */
const doc = new Document({
  creator: "EventSphere",
  title: "EventSphere Frontend — Architecture & Dependency Guide",
  description: "Beginner-friendly visual tour of every file and how they connect.",
  styles: {
    default: { document: { run: { font: "Calibri", size: 22 } } },
    paragraphStyles: [
      {
        id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 40, bold: true, color: COL.brand, font: "Calibri" },
        paragraph: { spacing: { before: 360, after: 180 }, outlineLevel: 0 },
      },
      {
        id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 28, bold: true, color: COL.accent, font: "Calibri" },
        paragraph: { spacing: { before: 240, after: 120 }, outlineLevel: 1 },
      },
    ],
  },
  numbering: {
    config: [
      {
        reference: "bullets",
        levels: [{
          level: 0, format: LevelFormat.BULLET, text: "•", alignment: AlignmentType.LEFT,
          style: { paragraph: { indent: { left: 720, hanging: 360 } } },
        }],
      },
    ],
  },
  sections: [{
    properties: {
      page: {
        size: { width: 12240, height: 15840 },
        margin: { top: 1080, right: 1080, bottom: 1080, left: 1080 },
      },
    },
    headers: {
      default: new Header({
        children: [new Paragraph({
          alignment: AlignmentType.RIGHT,
          children: [new TextRun({ text: "EventSphere Frontend — Architecture & Dependency Guide", size: 18, color: "9CA3AF" })],
        })],
      }),
    },
    footers: {
      default: new Footer({
        children: [new Paragraph({
          alignment: AlignmentType.RIGHT,
          children: [
            new TextRun({ text: "Page ", size: 18, color: "9CA3AF" }),
            new TextRun({ children: [PageNumber.CURRENT], size: 18, color: "9CA3AF" }),
            new TextRun({ text: " of ", size: 18, color: "9CA3AF" }),
            new TextRun({ children: [PageNumber.TOTAL_PAGES], size: 18, color: "9CA3AF" }),
          ],
        })],
      }),
    },
    children: content,
  }],
});

Packer.toBuffer(doc).then((buf) => {
  const out = "C:\\Users\\2485781\\Internship Project\\EventSphere-Architecture-Guide.docx";
  fs.writeFileSync(out, buf);
  console.log("Wrote: " + out + "  (" + buf.length + " bytes)");
});
