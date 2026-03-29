create table if not exists identity_event_store (
    aggregate_id uuid not null,
    version bigint not null,
    event_type varchar(100) not null,
    payload jsonb not null,
    occurred_at timestamptz not null,
    primary key (aggregate_id, version)
);

create table if not exists identity_user_projection (
    user_id uuid primary key,
    email varchar(320) not null unique,
    hashed_password text not null,
    verified boolean not null,
    invitation_token varchar(255),
    verification_token varchar(255),
    verification_token_expires_at timestamptz
);

alter table identity_user_projection
    add column if not exists invitation_token varchar(255);
alter table identity_user_projection
    add column if not exists reset_token varchar(255);
alter table identity_user_projection
    add column if not exists reset_token_expires_at timestamptz;

create table if not exists identity_refresh_tokens (
    token varchar(255) primary key,
    user_id uuid not null,
    expires_at timestamptz not null,
    invalidated boolean not null default false,
    invalidated_at timestamptz
);

create table if not exists identity_login_failed_events (
    id bigserial primary key,
    email varchar(320) not null,
    reason varchar(100) not null,
    payload jsonb not null,
    occurred_at timestamptz not null
);

create table if not exists groups_event_store (
    aggregate_id uuid not null,
    version bigint not null,
    event_type varchar(100) not null,
    payload jsonb not null,
    occurred_at timestamptz not null,
    primary key (aggregate_id, version)
);

create table if not exists groups_projection (
    group_id uuid primary key,
    name varchar(100) not null,
    currency varchar(10) not null,
    created_at timestamptz not null
);

create table if not exists group_members_projection (
    group_id uuid not null,
    user_id uuid not null,
    joined_at timestamptz not null,
    left_at timestamptz,
    primary key (group_id, user_id)
);

alter table group_members_projection
    add column if not exists left_at timestamptz;

create table if not exists group_invitations_projection (
    invitation_token varchar(255) primary key,
    group_id uuid not null,
    invited_email varchar(320),
    invited_by uuid not null,
    expires_at timestamptz not null,
    used boolean not null,
    used_by uuid,
    used_at timestamptz,
    created_at timestamptz not null
);

create table if not exists porra_event_store (
    aggregate_id uuid not null,
    version bigint not null,
    event_type varchar(100) not null,
    payload jsonb not null,
    occurred_at timestamptz not null,
    primary key (aggregate_id, version)
);

create table if not exists porra_projection (
    porra_id uuid primary key,
    group_id uuid not null,
    creator_id uuid not null,
    title varchar(200) not null,
    description text,
    type varchar(50) not null,
    tolerance double precision,
    stake integer not null,
    betting_deadline timestamptz not null,
    resolution_date timestamptz not null,
    visibility varchar(50) not null,
    bet_modifiable boolean not null,
    status varchar(50) not null,
    created_at timestamptz not null
);

create table if not exists porra_option_projection (
    porra_id uuid not null,
    option_value varchar(255) not null
);

create table if not exists porra_bet_projection (
    porra_id uuid not null,
    member_id uuid not null,
    prediction text not null,
    placed_at timestamptz not null,
    primary key (porra_id, member_id)
);

create table if not exists resolution_event_store (
    aggregate_id uuid not null,
    version bigint not null,
    event_type varchar(100) not null,
    payload jsonb not null,
    occurred_at timestamptz not null,
    primary key (aggregate_id, version)
);

create table if not exists resolution_projection (
    resolution_id uuid primary key,
    porra_id uuid not null,
    group_id uuid not null,
    proposer_id uuid not null,
    proposed_result text not null,
    waiting_ends_at timestamptz not null,
    status varchar(50) not null,
    created_at timestamptz not null,
    accepted_at timestamptz
);

alter table resolution_projection add column if not exists disputer_id uuid;
alter table resolution_projection add column if not exists dispute_reason text;
alter table resolution_projection add column if not exists disputed_at timestamptz;
alter table resolution_projection add column if not exists voting_ends_at timestamptz;
alter table resolution_projection add column if not exists accepted_result text;
alter table resolution_projection add column if not exists accepted_method varchar(50);
alter table resolution_projection add column if not exists proposal_type varchar(50);
alter table resolution_projection add column if not exists proposal_reason text;
alter table resolution_projection drop constraint if exists resolution_projection_porra_id_key;

create table if not exists resolution_vote_projection (
    resolution_id uuid not null,
    voter_id uuid not null,
    voted_result text not null,
    voted_at timestamptz not null,
    primary key (resolution_id, voter_id)
);

create table if not exists debt_event_store (
    aggregate_id uuid not null,
    version bigint not null,
    event_type varchar(100) not null,
    payload jsonb not null,
    occurred_at timestamptz not null,
    primary key (aggregate_id, version)
);

create table if not exists debt_projection (
    debt_id uuid primary key,
    group_id uuid not null,
    porra_id uuid not null,
    from_member_id uuid not null,
    to_member_id uuid not null,
    amount numeric(12, 2) not null,
    currency varchar(10) not null,
    status varchar(50) not null,
    created_at timestamptz not null,
    requested_at timestamptz,
    settled_at timestamptz
);

create table if not exists settlement_processed_porra_projection (
    porra_id uuid primary key,
    group_id uuid not null,
    outcome varchar(50) not null,
    processed_at timestamptz not null
);

create table if not exists settlement_member_porra_projection (
    group_id uuid not null,
    porra_id uuid not null,
    member_id uuid not null,
    title varchar(200) not null,
    type varchar(50) not null,
    my_bet text,
    result text,
    outcome varchar(50) not null,
    money_change numeric(12, 2) not null,
    currency varchar(10) not null,
    resolved_at timestamptz not null,
    primary key (porra_id, member_id)
);
