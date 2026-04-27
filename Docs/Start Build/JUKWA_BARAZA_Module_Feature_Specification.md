# JUKWA × JAMII IMARA MASHINANI (JIM)
# Feature Specification: The Governance Accountability Engine

**Version 1.0 | March 2026**
**Module Codename: BARAZA**

---

## Executive Summary

Jamii Imara Mashinani — meaning "Strong Community at the Grassroots" — is a Kenyan government initiative that physically relocates senior officials (Cabinet Secretaries, Principal Secretaries, CEOs of state agencies) to villages and towns for face-to-face citizen engagement. Spearheaded by PS Mary Muthoni under the State Department for Public Health and Professional Standards, JIM has conducted 12 citizen engagement forums across 10 counties by March 2026, operating on a "whole-of-government" approach where citizens raise issues spanning health, infrastructure, security, agriculture, land, and employment in a single session. Its digital component (jamiiimara.org) provides a ticketing system for citizens to submit grievances and track responses.

This feature specification defines how Jukwa absorbs, extends, and digitally supercharges the JIM model into a permanent, always-on governance accountability infrastructure that we are calling the BARAZA module. The integration transforms JIM from a periodic, geographically limited physical event into a continuous digital-physical hybrid where every citizen in every ward effectively has a permanent seat at the baraza table — and where every promise made by every official is tracked, timestamped, and publicly accountable until fulfilled.

The synthesis is powerful because the two platforms address each other's fundamental weaknesses. JIM's weakness is discontinuity: forums happen periodically, officials visit and leave, and the "follow-through test" (does the stalled road actually get paved after the baraza?) depends on institutional memory and political will. Jukwa's weakness is government responsiveness: citizen reports enter a digital pipeline, but without institutional commitment, they risk being ignored. When merged, JIM provides the political authority and institutional weight (Cabinet Secretaries making promises in front of 1,000 residents), while Jukwa provides the persistent digital memory that ensures those promises cannot be quietly forgotten.

---

## 1. The Problem This Feature Solves

Kenya's governance accountability gap has a specific shape. It is not primarily a communication gap — the government runs Huduma Centres, eCitizen, GavaConnect, the President's Delivery Unit, MyGov, county public participation forums, and now JIM itself. The gap is in what happens between the conversation and the outcome. A citizen raises the issue of missing drugs at the local hospital during a JIM baraza in Isiolo. The PS nods, takes notes, assigns a Ticket ID. Six months later, the drugs are still missing, the Ticket ID leads nowhere, and the citizen has no mechanism to escalate, no public record that the promise was made, and no way to know whether other citizens in other counties raised the same issue and received the same empty promises.

This is the accountability collapse that Jukwa's BARAZA module resolves. Every commitment made at every JIM forum — and between forums, every citizen grievance submitted digitally — enters an immutable, publicly visible pipeline where the clock starts ticking the moment the promise is made, where resolution milestones are tracked in real time, where citizens receive push notifications when their issue status changes, where aggregate government response performance is published ward by ward and agency by agency, and where persistent non-resolution triggers automatic escalation through defined chains of command.

The result is not adversarial. It is the infrastructure of trust. When the government delivers on its commitments and citizens can see the evidence, trust grows. When it does not, the data speaks for itself, and officials face informed, evidence-based civic pressure rather than generalized frustration. PS Muthoni herself articulated this vision when she described JIM as a platform for "building trust between citizens and government, promoting transparency and ensuring development initiatives are driven by grassroots priorities." The BARAZA module simply provides the persistent digital architecture to make that vision operationally real between forum visits.

---

## 2. Feature Architecture: The Commitment Lifecycle

The BARAZA module introduces a new first-class entity into Jukwa's data model: the **Government Commitment**. Unlike an incident report (which describes something that happened) or a civic action (which describes something citizens want to do), a Commitment describes something the government has promised to do. This distinction is architecturally important because commitments carry different metadata, different lifecycle states, different accountability rules, and different public visibility requirements.

### 2.1 The Commitment Object

Every commitment captured by the BARAZA module contains the following structured data:

**Origin Context** records how the commitment was created. A commitment can originate from a physical JIM baraza (captured by a trained Field Digitizer using the Jukwa app's Baraza Mode, or ingested from the jamiiimara.org ticketing system via API integration), from a citizen's digital submission through Jukwa (a report that generates a government response containing a specific promise), or from a government official's proactive announcement (a road project timeline, a hospital staffing pledge, a water infrastructure commitment). The origin type determines the initial evidence chain: a baraza commitment includes the date, location, attending officials, and ideally audio/video reference; a digital commitment includes the submission timestamp, response text, and responding officer's designation.

**Responsible Parties** identifies the specific government entity and, where possible, the individual official who made or owns the commitment. The module maintains a structured directory of government entities — all 22 Ministries, their State Departments, Principal Secretaries, key parastatals (NTSA, NEMA, KURA, KeNHA, KPLC, KEMSA, SHA), and all 47 County Governments with their departments. When PS Muthoni's team visits Isiolo and the KEMSA representative promises a resupply of essential medicines within 30 days, the commitment is tagged to KEMSA as the responsible agency, to the specific official who spoke, and to Isiolo County Hospital as the affected facility. This specificity prevents the diffusion of responsibility that typically kills government accountability.

**The Promise** is the structured core: what was promised, to whom (which ward, community, or facility), by when (explicit deadline if stated, or system-assigned default SLA based on commitment category), and what constitutes verifiable fulfillment (drugs delivered and verified by hospital staff, road section graded and photographed, water point operational and flowing). The fulfillment criteria are critical — they transform a vague political statement into a measurable deliverable. Where the original promise was vague ("we will look into this"), the system assigns it to a "Clarification Required" state and generates a follow-up request to the responsible agency for a specific, measurable commitment.

**The Evidence Chain** accumulates over the commitment's lifecycle. It begins with the creation evidence (baraza recording, digital submission, official announcement) and grows as the government provides progress updates, as citizens submit verification evidence (a photo of the repaired road, a video of the functioning water point, a screenshot of hospital drug stock), and as the system records milestone completions and deadline adherence or breach.

### 2.2 Commitment Lifecycle States

A commitment moves through a defined state machine that governs notifications, escalations, and public visibility at each stage:

```
                                     ┌──────────────┐
                                     │   CAPTURED    │
                                     │ (Baraza/App)  │
                                     └──────┬───────┘
                                            │
                                            ▼
                                     ┌──────────────┐
                                     │  CLASSIFIED   │
                                     │ Agency Routed │
                                     └──────┬───────┘
                                            │
                              ┌─────────────┼─────────────┐
                              ▼             ▼             ▼
                     ┌──────────────┐ ┌───────────┐ ┌─────────────┐
                     │ ACKNOWLEDGED │ │  SILENCE   │ │ CLARIFICATION│
                     │ Agency Owns  │ │ (No reply) │ │  REQUIRED    │
                     └──────┬───────┘ └─────┬─────┘ └──────┬──────┘
                            │               │              │
                            ▼               ▼              │
                     ┌──────────────┐ ┌───────────┐        │
                     │ IN PROGRESS  │ │ ESCALATED │◄───────┘
                     │ Updates Due  │ │ To Senior │
                     └──────┬───────┘ └─────┬─────┘
                            │               │
                            ▼               ▼
                     ┌──────────────┐ ┌───────────────┐
                     │  FULFILLED   │ │   OVERDUE     │
                     │ (Pending     │ │ Public Alert   │
                     │  Citizen     │ │ Triggered      │
                     │  Verify)     │ └───────┬───────┘
                     └──────┬───────┘         │
                            │                 ▼
                            ▼          ┌───────────────┐
                     ┌──────────────┐  │  FAILED       │
                     │   VERIFIED   │  │  (Unfulfilled │
                     │   RESOLVED   │  │   + Expired)  │
                     └──────────────┘  └───────────────┘
```

**CAPTURED** is the initial state when a commitment enters the system. The AI Classification Engine (shared with Jukwa's existing Incident Service) categorizes it by sector (health, infrastructure, water, security, agriculture, education, land, employment) and assigns it to the responsible government entity using the same intelligent routing logic that handles incident reports.

**CLASSIFIED** confirms routing. The commitment is now visible on the public Accountability Dashboard and begins its SLA clock. The responsible agency receives a formal notification via the government integration API (or email/SMS where API integration is pending).

**ACKNOWLEDGED** means the agency has formally accepted ownership. This is the government's opportunity to refine the commitment — to provide a specific timeline, to clarify scope, or to redirect to the correct entity if misrouted. Acknowledgment must occur within 72 hours; failure to acknowledge triggers automatic escalation.

**IN PROGRESS** indicates active work. The agency is expected to provide progress updates at defined intervals based on commitment category: weekly for emergency/health commitments, biweekly for infrastructure, monthly for policy/systemic issues. Each missed update generates a "Progress Overdue" notification visible to the originating citizen and on the public dashboard.

**FULFILLED** is the agency's claim that the commitment has been met. This triggers the citizen verification loop: the originating citizen (or, for baraza commitments, any citizen in the affected ward) receives a notification asking them to confirm fulfillment, ideally with evidence (photo, visit confirmation). Crowd-verification from multiple citizens in the area strengthens the confirmation.

**VERIFIED RESOLVED** is the terminal success state, reached when citizen verification confirms government fulfillment. This generates positive public data — the agency's resolution rate improves, the ward's civic health score increases, and the resolving official accrues accountability credit.

**SILENCE**, **ESCALATED**, **OVERDUE**, and **FAILED** represent the accountability pressure states. Silence (no acknowledgment within 72 hours) escalates to the agency head. Overdue (deadline passed without fulfillment) triggers a public alert visible on the ward's Accountability Dashboard. Failed (significantly overdue with no credible progress) marks the commitment as a documented government failure, permanently visible in the ward's civic record. These states are not punitive mechanisms — they are information. Citizens and journalists and oversight bodies use this information as they see fit.

### 2.3 The Accountability Clock

Every commitment carries a visible countdown timer from the moment it enters the CLASSIFIED state. Default SLAs are calibrated to realistic government timescales:

Emergency commitments (drug shortages at hospitals, broken water mains, security threats) carry a 7-day SLA. Infrastructure commitments (road repairs, building construction, facility upgrades) carry a 90-day SLA for planning acknowledgment and category-specific construction timelines thereafter. Service delivery commitments (staffing, equipment, process improvements) carry a 30-day SLA. Policy or systemic commitments (regulatory changes, program expansions, budget allocations) carry a 180-day SLA.

When an official at a JIM baraza makes a specific time commitment ("these drugs will be restocked within two weeks"), that explicit timeline overrides the default SLA and becomes the binding accountability clock. The Baraza Mode capture interface specifically prompts the Field Digitizer to record any stated timelines.

---

## 3. The Baraza Mode: Digitizing Physical Forums

### 3.1 Field Digitizer Interface

When JIM forums occur in the field, a trained team member (the "Field Digitizer") operates the Jukwa app in a specialized Baraza Mode designed for rapid, structured capture during live town-hall sessions. The interface is optimized for speed and noise — a village baraza with 1,000 residents, multiple speakers, and overlapping conversations is not a quiet office environment.

Baraza Mode presents a streamlined single-screen capture form: one tap to start audio recording (which runs continuously as a session record), a quick-select category wheel (health, roads, water, security, land, education, employment, other), a text field for the commitment summary (which supports voice-to-text in both English and Kiswahili via on-device speech recognition), a tag field for the responsible official's name and designation, and a large "CAPTURE" button that timestamps and saves each commitment as a discrete record within the ongoing baraza session.

A skilled Field Digitizer can capture 30 to 50 discrete commitments during a typical 3-hour baraza session. The session record (continuous audio plus structured commitment entries) is stored locally and syncs to the server when connectivity permits. Post-session, the editorial team reviews and refines the captured commitments — correcting transcription errors, linking to specific agencies, and verifying official names and designations — before the commitments enter the public pipeline.

### 3.2 Citizen Self-Capture

Not every governance interaction happens at an official JIM baraza. County public participation forums, ward-level barazas organized by MCAs, chief's meetings, and even impromptu encounters between officials and citizens all produce government commitments. Any Jukwa user can activate Baraza Mode to capture a commitment made by any government official in any setting. The citizen records the audio/video, enters the commitment details, identifies the official (from a searchable directory or by typing the name), and submits.

Citizen-captured commitments enter the same pipeline but carry a "Citizen Reported" origin tag and undergo community verification before publication. Other Jukwa users in the same ward can corroborate the commitment ("I was also at the chief's baraza and confirm the DO promised road grading by end of month"). Three independent corroborations from users in the affected ward promote the commitment to "Verified Capture" status, equivalent in pipeline treatment to an official Field Digitizer capture.

### 3.3 Integration with jamiiimara.org

For commitments already captured in JIM's existing digital ticketing system, Jukwa provides an API integration that imports Ticket IDs, grievance descriptions, assigned agencies, and any status updates into the BARAZA module. This prevents duplication and ensures citizens who submitted via jamiiimara.org see their existing tickets enriched with Jukwa's accountability tracking, public visibility, and escalation mechanisms. The integration is bidirectional: status updates generated within Jukwa (agency acknowledgments, progress reports, citizen verifications) flow back to jamiiimara.org so officials using that system see the same data.

---

## 4. The Accountability Dashboard: Public Civic Intelligence

The BARAZA module's most visible output is the Accountability Dashboard, a publicly accessible, real-time visualization of government commitment performance at every level of geographic and institutional granularity.

### 4.1 Ward-Level View

Every ward in Kenya gets a dedicated accountability page showing all active commitments affecting that ward, their current states, their countdown timers, and their resolution history. The ward's aggregate civic health is expressed as a simple, intuitive scorecard: total commitments made versus fulfilled, average resolution time, and a trend arrow (improving, stable, or declining). Citizens in the ward see their specific submissions and their status, but the aggregate data is public and accessible to anyone — journalists, researchers, opposition politicians, development partners, and other citizens.

### 4.2 Agency-Level View

Every government agency's commitment performance is aggregated and publicly visible. KEMSA's drug restocking commitments across all counties. KURA's road repair commitments across all wards. The SHA's enrollment and access commitments. Each agency page shows total commitments, fulfillment rate, average resolution time, number of overdue commitments, and number of silences (commitments never acknowledged). This is the "civic insights" data that transforms Jukwa into an independent accountability watchdog — not through opinion or advocacy, but through transparent, verifiable, citizen-confirmed data.

### 4.3 Official-Level View

Individual officials who make commitments at JIM barazas accumulate a public performance record. PS Muthoni's commitments across her 12 forum visits, and their fulfillment status, are visible in aggregate. This is not a "name and shame" mechanism — officials who fulfill their commitments accumulate positive accountability credit that demonstrates their effectiveness to constituents and superiors. The incentive structure rewards delivery, not promises.

### 4.4 National Heatmap

The broadest view renders Kenya's 1,450 wards as a color-coded heatmap of governance responsiveness. Green wards have high fulfillment rates and short resolution times. Red wards have chronic overdue commitments and government silence. This visualization, updated in real time, provides the most powerful single image of Kenya's governance performance ever produced — and it is built entirely from citizen-verified data, not government self-reporting.

---

## 5. The Civic Education Engine

JIM's physical barazas serve a vital civic education function: officials explain government structures, helping residents understand which specific office handles which services. The BARAZA module digitizes and extends this function into a permanent, always-accessible civic knowledge base within the Jukwa app.

### 5.1 The "Nani Anashughulikia Nini?" (Who Handles What?) Directory

When a citizen opens the Jukwa app to report an issue or track a commitment, the BARAZA module provides a contextual civic education layer. If the citizen is reporting a pothole, the app briefly explains that urban road maintenance falls under the Kenya Urban Roads Authority (KURA) for national roads or the County Roads Department for county roads, and that the distinction depends on the road's classification. This is not a lecture — it is a single expandable information card that appears alongside the report form, teaching the citizen something useful about how their government works while they are actively engaging with it.

The directory is structured as a searchable knowledge base: "Who fixes streetlights in Nairobi?" → Kenya Power (national grid) or Nairobi County (county-installed). "Who handles drug supply to hospitals?" → KEMSA (procurement and distribution) plus the County Health Department (facility management). "Where does my property rate payment go?" → County Government Revenue Department. This addresses one of JIM's core civic education goals — demystifying the maze of national versus county mandates — in a format accessible to every smartphone user every day, not just the 1,000 residents who attend a periodic baraza.

### 5.2 Contextual Push Education

The BARAZA module opportunistically pushes civic education content based on user activity and trending civic issues. If SHA enrollment is a trending topic in the user's county (as it has been in multiple JIM forums), the app surfaces a clear, jargon-free explainer on SHA: what it covers, how to enroll, what the contribution schedule is, and where to access services. If the user's ward has a high concentration of land-related commitments, the app explains the difference between National Land Commission jurisdiction and county land offices. This content is written in partnership with government communications teams and reviewed for accuracy, then delivered in both English and Kiswahili using natural, conversational language.

---

## 6. The Whole-of-Government Bridge

JIM's operational innovation is the "whole-of-government" approach: instead of citizens navigating siloed ministries, a single forum brings multiple agencies to the table simultaneously. A citizen in Isiolo raises health, roads, water, security, and employment concerns in a single session, and the relevant officials respond in real time.

The BARAZA module replicates this in digital form through its multi-agency commitment routing. When a citizen submits a grievance that spans multiple sectors (for example, "the road to our dispensary is impassable during rains, and when we arrive the dispensary has no drugs"), the AI Classification Engine identifies it as a compound issue and generates multiple linked commitments: one to KURA or the county roads department for the road, one to KEMSA for drug supply, and one to the county health department for facility management. The commitments are linked so that all parties can see the compound nature of the problem, and the citizen sees a unified view of all related actions.

This digital whole-of-government routing solves one of JIM's fundamental scaling constraints. PS Muthoni's team can physically visit perhaps 20 to 25 counties per year. Jukwa's BARAZA module operates in all 47 counties, all 1,450 wards, 24 hours a day, 365 days a year. Every citizen interaction becomes, in effect, a mini-baraza, with the same multi-agency routing that JIM achieves through physical convening.

---

## 7. The Follow-Through Engine: Solving JIM's Biggest Challenge

Public analysis of JIM consistently identifies the "follow-through test" as the initiative's critical vulnerability. Forums generate energy and promises, but the real impact is measured months later by whether the stalled road gets paved, the hospital gets drugs, the water point gets fixed. This is precisely the gap that digital infrastructure can close.

### 7.1 Automated Escalation Chains

When a commitment enters the OVERDUE state, the BARAZA module triggers a structured escalation sequence. The first escalation (at deadline + 7 days) sends a formal notice to the responsible agency's head, copied to the PS of the parent ministry. The second escalation (at deadline + 30 days) publishes the overdue commitment to a dedicated "Government Watch" feed visible to all Jukwa users nationally, to subscribed journalists and civil society organizations, and to the ward's MCA. The third escalation (at deadline + 90 days) flags the commitment in the agency's public accountability scorecard as a documented failure and triggers an automated report to the relevant parliamentary committee's clerk.

This escalation chain is not speculative — it mirrors existing Kenyan accountability mechanisms (parliamentary committee oversight, media scrutiny, MCA constituent accountability) but automates the information flow that typically breaks down between a village baraza and a committee room in Parliament.

### 7.2 Citizen Verification Network

Government self-reporting of fulfillment is inherently unreliable. An agency claiming it has restocked a hospital's drug supply does not mean the drugs actually arrived, are in the correct quantities, or are accessible to patients. The BARAZA module resolves this through the Citizen Verification Network.

When an agency reports a commitment as fulfilled, the module pushes a verification request to all Jukwa users in the affected ward. The request asks a simple question: "The government says [specific commitment] has been completed. Can you confirm?" Users respond with a thumbs up or down, optionally attaching photo or video evidence. Five independent confirmations from unique devices within the ward constitute "Citizen Verified" status. Five independent denials trigger a "Disputed" status that sends the commitment back to IN PROGRESS with a flag for the agency to address the discrepancy.

This crowd-verification model draws directly from Ushahidi's proven triangulation methodology, where report reliability is determined by volume and geographic consistency rather than individual identity verification. It also incentivizes genuine fulfillment — agencies that falsely report completion face the reputational cost of public dispute by their own constituents.

### 7.3 Longitudinal Impact Tracking

The BARAZA module tracks not just individual commitment resolution but long-term patterns. If Kisii County's road commitments have a 23% fulfillment rate over 12 months while Nyamira County's sit at 67%, that pattern tells a story about institutional capacity, political will, or resource allocation that no single baraza can surface. Quarterly "State of Accountability" reports — generated automatically from the platform's data — provide this longitudinal view at national, county, and ward levels. These reports are public, downloadable, and designed to be useful to parliament, media, development partners, and citizens conducting informed civic engagement.

---

## 8. Technical Integration with Existing Jukwa Architecture

### 8.1 Database Extensions

The BARAZA module extends Jukwa's existing PostgreSQL/PostGIS schema with the following additions:

```sql
-- Government Commitments (core BARAZA entity)
CREATE TABLE commitments (
    commitment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    origin_type VARCHAR(30) NOT NULL,
        -- 'JIM_BARAZA', 'CITIZEN_CAPTURE', 'DIGITAL_SUBMISSION',
        -- 'OFFICIAL_ANNOUNCEMENT', 'JIM_TICKET_IMPORT'
    baraza_session_id UUID REFERENCES baraza_sessions(session_id),
    originating_citizen_id UUID REFERENCES citizens(citizen_id),
    jim_ticket_id VARCHAR(50),

    -- The Promise
    sector VARCHAR(50) NOT NULL,
    promise_summary TEXT NOT NULL,
    fulfillment_criteria TEXT,
    affected_ward_id INTEGER REFERENCES wards(ward_id),
    affected_location GEOMETRY(Point, 4326),
    affected_facility_name VARCHAR(200),

    -- Responsibility
    responsible_agency_id UUID REFERENCES government_agencies(agency_id),
    responsible_official_name VARCHAR(200),
    responsible_official_designation VARCHAR(200),

    -- Lifecycle
    status VARCHAR(30) DEFAULT 'CAPTURED',
    sla_deadline TIMESTAMPTZ,
    explicit_deadline TIMESTAMPTZ,
    acknowledged_at TIMESTAMPTZ,
    fulfilled_claimed_at TIMESTAMPTZ,
    citizen_verified_at TIMESTAMPTZ,
    escalation_level SMALLINT DEFAULT 0,

    -- Metadata
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_commitments_ward ON commitments (affected_ward_id, status);
CREATE INDEX idx_commitments_agency ON commitments (responsible_agency_id, status);
CREATE INDEX idx_commitments_status ON commitments (status, sla_deadline);
CREATE INDEX idx_commitments_location ON commitments USING GIST (affected_location);

-- Baraza Sessions (physical JIM forums + citizen-captured events)
CREATE TABLE baraza_sessions (
    session_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_type VARCHAR(30) NOT NULL,
        -- 'JIM_OFFICIAL', 'COUNTY_FORUM', 'WARD_BARAZA', 'CITIZEN_CAPTURED'
    county_name VARCHAR(50) NOT NULL,
    ward_id INTEGER REFERENCES wards(ward_id),
    location GEOMETRY(Point, 4326),
    venue_name VARCHAR(200),
    session_date DATE NOT NULL,
    attending_officials JSONB,
    audio_recording_url TEXT,
    total_commitments_captured INTEGER DEFAULT 0,
    digitizer_citizen_id UUID REFERENCES citizens(citizen_id),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Government Agency Directory
CREATE TABLE government_agencies (
    agency_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agency_name VARCHAR(200) NOT NULL,
    agency_type VARCHAR(50) NOT NULL,
        -- 'MINISTRY', 'STATE_DEPARTMENT', 'PARASTATAL',
        -- 'COUNTY_GOVT', 'COUNTY_DEPT'
    parent_agency_id UUID REFERENCES government_agencies(agency_id),
    contact_email VARCHAR(200),
    contact_phone VARCHAR(20),
    api_endpoint TEXT,
    default_sla_days JSONB
);

-- Commitment Evidence Chain
CREATE TABLE commitment_evidence (
    evidence_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    commitment_id UUID REFERENCES commitments(commitment_id),
    evidence_type VARCHAR(30) NOT NULL,
        -- 'CREATION_AUDIO', 'CREATION_VIDEO', 'PROGRESS_UPDATE',
        -- 'AGENCY_REPORT', 'CITIZEN_VERIFICATION', 'CITIZEN_DISPUTE',
        -- 'ESCALATION_NOTICE'
    submitted_by_type VARCHAR(20) NOT NULL,
        -- 'FIELD_DIGITIZER', 'CITIZEN', 'AGENCY', 'SYSTEM'
    submitted_by_id UUID,
    content TEXT,
    media_urls TEXT[],
    submitted_at TIMESTAMPTZ DEFAULT NOW()
);

-- Citizen Verification Votes
CREATE TABLE commitment_verifications (
    verification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    commitment_id UUID REFERENCES commitments(commitment_id),
    citizen_id UUID REFERENCES citizens(citizen_id),
    vote VARCHAR(10) NOT NULL, -- 'CONFIRMED', 'DISPUTED'
    evidence_media_url TEXT,
    comment TEXT,
    voted_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(commitment_id, citizen_id)
);

-- Accountability Scorecard (materialized, refreshed hourly)
CREATE MATERIALIZED VIEW agency_scorecards AS
SELECT
    a.agency_id,
    a.agency_name,
    COUNT(c.commitment_id) AS total_commitments,
    COUNT(c.commitment_id) FILTER
        (WHERE c.status = 'VERIFIED_RESOLVED') AS fulfilled,
    COUNT(c.commitment_id) FILTER
        (WHERE c.status = 'FAILED') AS failed,
    COUNT(c.commitment_id) FILTER
        (WHERE c.status = 'OVERDUE') AS overdue,
    COUNT(c.commitment_id) FILTER
        (WHERE c.status = 'SILENCE') AS silenced,
    ROUND(
        100.0 * COUNT(c.commitment_id)
            FILTER (WHERE c.status = 'VERIFIED_RESOLVED')
        / NULLIF(COUNT(c.commitment_id), 0), 1
    ) AS fulfillment_rate_pct,
    AVG(
        EXTRACT(EPOCH FROM (c.citizen_verified_at - c.created_at)) / 86400
    ) FILTER (WHERE c.status = 'VERIFIED_RESOLVED') AS avg_resolution_days
FROM government_agencies a
LEFT JOIN commitments c ON c.responsible_agency_id = a.agency_id
GROUP BY a.agency_id, a.agency_name;
```

### 8.2 Service Integration

The BARAZA module adds two new microservices to Jukwa's Application Services Tier:

The **Commitment Service** manages the full commitment lifecycle: creation, classification, routing, state transitions, escalation timers, and citizen verification. It publishes events to the MQTT broker on `jukwa/baraza/{ward_id}/commitments` topics, enabling real-time dashboard updates and push notifications. It integrates with the existing AI Classification Engine for sector categorization and agency routing — the same intelligence that routes incident reports to NPS or NTSA now routes government commitments to KEMSA or KURA.

The **Accountability Service** generates the public dashboards, scorecards, and longitudinal reports. It refreshes materialized views hourly, computes ward-level civic health scores, triggers automated escalation actions on overdue commitments, and generates the quarterly "State of Accountability" reports. It exposes a public read-only API for journalists, researchers, and civil society organizations to query accountability data programmatically.

### 8.3 MQTT Topic Extensions

The BARAZA module adds the following to Jukwa's MQTT topic hierarchy:

```
jukwa/baraza/{county}/sessions          — New baraza session announcements
jukwa/baraza/{ward_id}/commitments      — New and updated commitments
jukwa/baraza/{ward_id}/verifications    — Citizen verification requests
jukwa/baraza/agencies/{agency_id}       — Agency-specific commitment updates
jukwa/baraza/escalations                — Escalation events (restricted)
jukwa/baraza/national/scorecards        — Aggregate scorecard updates
```

### 8.4 Client UI Additions

The native Android app gains three new primary screens within the BARAZA section:

The **My Ward's Baraza** screen shows all active commitments affecting the user's ward, their states, their countdown timers, and pending verification requests. It is the citizen's primary interface for tracking what the government has promised their community and whether those promises are being kept.

The **Capture Commitment** screen (Baraza Mode) provides the rapid-capture interface for Field Digitizers and citizen self-capture, as described in Section 3.

The **Accountability Explorer** screen provides the searchable, filterable dashboard for browsing agency scorecards, official performance records, and the national heatmap. It supports drill-down from national to county to ward to individual commitment.

All three screens follow the existing SDUI pattern, with server-driven layout variants for different device tiers, and are accessible via USSD in simplified form (citizens can check commitment status by entering their Ticket ID via USSD shortcode).

---

## 9. Privacy Considerations for the BARAZA Module

The BARAZA module handles data that is fundamentally different from Jukwa's incident reporting: it is about government performance, not citizen vulnerability. This shifts the privacy calculus.

Government commitments, agency performance data, and official accountability records are **public data by design**. There is no privacy interest in hiding whether KEMSA restocked a public hospital — this is public service delivery that citizens have a constitutional right to scrutinize (Article 35, right of access to information; Article 10, national values of transparency and accountability).

Citizen identity within the BARAZA module follows Jukwa's existing graduated model. Citizens who submit grievances or vote in verification can do so pseudonymously (Standard Mode) or anonymously (Incognito Mode). Their individual votes and submissions are not publicly linked to identities. Only the aggregate verification outcome is published ("12 citizens confirmed, 3 disputed"). Field Digitizers are identified by role (not personal identity) in baraza session records.

The one area requiring careful handling is the Government Agency Directory's individual official data. Names and designations of public officials are legitimately public (they appear in government gazettes, official websites, and media coverage of JIM forums). However, the module should not store personal contact information for officials — only institutional contact channels (office email, department phone number, official social media handles).

---

## 10. Sustainability and Civic Power

The BARAZA module dramatically strengthens Jukwa's sustainability model and civic influence.

The aggregated accountability data becomes the platform's most valuable asset for government engagement. When Jukwa approaches a county government to propose formal integration, it arrives not with a sales pitch but with data: "Your county has 347 active commitments, a 34% fulfillment rate, and an average resolution time of 67 days. Here is how the platform can help you improve those numbers." This is the "civic insights as leverage" concept from the original Gemini contribution, now grounded in structured, verifiable commitment data rather than abstract report aggregations.

Development partners (World Bank, USAID, DFID, UN agencies) routinely fund governance accountability programs. Jukwa's BARAZA module provides measurable, real-time governance performance data at a granularity that no current monitoring system in Kenya achieves. This positions the platform as infrastructure for development accountability, opening funding pathways that a pure civic tech app would not access.

Media partnerships generate both visibility and revenue. News organizations can embed Jukwa's accountability widgets (ward scorecards, agency rankings, overdue commitment alerts) in their reporting, with attribution and partnership branding. A journalist covering Isiolo County can instantly pull KEMSA's drug supply fulfillment rate for the county, compare it to the national average, and cite specific unfulfilled commitments — all sourced from citizen-verified data.

---

## 11. Alignment with Kenya's Existing Governance Framework

The BARAZA module does not create a parallel governance structure. It digitizes and strengthens existing mechanisms mandated by the Constitution of Kenya 2010.

**Article 10** establishes national values including transparency, accountability, and public participation — BARAZA provides the infrastructure for operationalizing these principles.

**Article 35** guarantees every citizen the right of access to information held by the State and the right to correction of untrue or misleading information — the Accountability Dashboard fulfills this right by making government commitment performance publicly accessible.

**Article 174** establishes the objects of devolution, including to "give powers of self-governance to the people and enhance their participation in the exercise of the powers of the State" — BARAZA's ward-level engagement, civic education, and verification network directly serve this constitutional objective.

**The County Governments Act 2012 (Part VIII)** mandates public participation in county governance — BARAZA provides the digital infrastructure for continuous, scalable public participation beyond periodic physical forums.

**The Access to Information Act 2016** creates a framework for proactive disclosure of government information — the Accountability Dashboard implements proactive disclosure of government commitment performance.

By anchoring the module in existing constitutional and statutory mandates, Jukwa positions BARAZA not as activism or opposition but as digital governance infrastructure that any administration benefits from adopting.

---

## 12. Summary: What Changes in Jukwa with BARAZA

Before BARAZA, Jukwa was a powerful citizen-to-government communication and reporting platform. Citizens report problems, the system routes them to agencies, and transparency mechanisms create pressure for response. This is valuable, but it is fundamentally reactive.

With BARAZA, Jukwa becomes a complete governance accountability cycle. Citizens report problems (existing Incident Service). The government responds with commitments (new Commitment Service). Those commitments are tracked against deadlines (Accountability Clock). Government claims of fulfillment are verified by citizens on the ground (Citizen Verification Network). Performance is publicly scored and historically tracked (Accountability Dashboard). Non-performance is escalated through institutional channels (Follow-Through Engine). And the aggregate data produces civic intelligence that informs national governance discourse (Civic Insights).

The grandmother in Kisii who raised her hand at PS Muthoni's baraza and the Gen Z developer in Westlands who filed a report through the app now operate within the same accountability ecosystem. The grandmother's concern enters the same pipeline, is tracked by the same clock, and is verified by the same community network. The government official who made a promise in a village baraza faces the same public scorecard as the agency that responded to a digital report. The physical and digital merge into a single, continuous, inescapable accountability infrastructure.

This is what PS Muthoni's vision of "building trust between citizens and government" looks like when it has persistent digital architecture behind it. Trust is not built by a single baraza visit — it is built by consistent, verifiable follow-through on every promise, in every ward, tracked transparently and permanently.

---

*This feature specification is a component of the JUKWA Architectural & Structural Integration Framework v1.0. Implementation follows the phased roadmap defined in the parent document, with BARAZA elements integrated into Phases 2 through 4.*
