/* eslint-disable  @typescript-eslint/no-explicit-any */
abstract class ApiBase {
	abstract _call(path: string, request: any): Promise<any>;
	async v1_tunnels_list(): Promise<ApiResultNoFail<AccountTunnelsV1>> {
		return await this._call("/v1/tunnels/list", {}) as ApiResultNoFail<AccountTunnelsV1>;
	}
	async v1_tunnels_create(req: ReqTunnelsCreateV1): Promise<ApiResult<ObjectId, TunnelCreateErrorV1>> {
		return await this._call("/v1/tunnels/create", req) as ApiResult<ObjectId, TunnelCreateErrorV1>;
	}
	async v1_schemas_get(req: ReqSchemasGetV1): Promise<ApiResult<SchemaData, SchemaGetError>> {
		return await this._call("/v1/schemas/get", req) as ApiResult<SchemaData, SchemaGetError>;
	}
	async v1_tunnels_config(req: ReqTunnelsConfigV1): Promise<ApiResult<undefined, TunnelConfigError>> {
		return await this._call("/v1/tunnels/config", req) as ApiResult<undefined, TunnelConfigError>;
	}
	async v1_tunnels_propset(req: ReqTunnelsPropset): Promise<ApiResult<undefined, TunnelPropSetError>> {
		return await this._call("/v1/tunnels/propset", req) as ApiResult<undefined, TunnelPropSetError>;
	}
	async v1_tunnels_typeset(req: ReqTunnelsTypeset): Promise<ApiResult<undefined, TunnelTypeSetError>> {
		return await this._call("/v1/tunnels/typeset", req) as ApiResult<undefined, TunnelTypeSetError>;
	}
	async v1_agents_rundata(): Promise<ApiResultNoFail<AgentRunDataV1>> {
		return await this._call("/v1/agents/rundata", {}) as ApiResultNoFail<AgentRunDataV1>;
	}
	async info_pops(): Promise<ApiResultNoFail<PlayitPops>> {
		return await this._call("/info/pops", {}) as ApiResultNoFail<PlayitPops>;
	}
	async login_signin(req: ReqLoginSignin): Promise<ApiResult<WebSession, SigninFail>> {
		return await this._call("/login/signin", req) as ApiResult<WebSession, SigninFail>;
	}
	async login_clearcookie(): Promise<ApiResultNoFail<ClearWebSession>> {
		return await this._call("/login/clearcookie", {}) as ApiResultNoFail<ClearWebSession>;
	}
	async login_create_guest(): Promise<ApiResult<WebSession, LoginCreateGuestError>> {
		return await this._call("/login/create/guest", {}) as ApiResult<WebSession, LoginCreateGuestError>;
	}
	async login_guest(): Promise<ApiResult<WebSession, GuestLoginError>> {
		return await this._call("/login/guest", {}) as ApiResult<WebSession, GuestLoginError>;
	}
	async login_reset_password(req: ReqLoginResetPassword): Promise<ApiResult<WebSession, PasswordResetError>> {
		return await this._call("/login/reset/password", req) as ApiResult<WebSession, PasswordResetError>;
	}
	async login_reset_send(req: ReqLoginResetSend): Promise<ApiResultNoFail<undefined>> {
		return await this._call("/login/reset/send", req) as ApiResultNoFail<undefined>;
	}
	async tunnels_create(req: ReqTunnelsCreate): Promise<ApiResult<ObjectId, TunnelCreateError>> {
		return await this._call("/tunnels/create", req) as ApiResult<ObjectId, TunnelCreateError>;
	}
	async tunnels_list(req: ReqTunnelsList): Promise<ApiResultNoFail<AccountTunnels>> {
		return await this._call("/tunnels/list", req) as ApiResultNoFail<AccountTunnels>;
	}
	async tunnels_update(req: ReqTunnelsUpdate): Promise<ApiResult<undefined, UpdateError>> {
		return await this._call("/tunnels/update", req) as ApiResult<undefined, UpdateError>;
	}
	async tunnels_delete(req: ReqTunnelsDelete): Promise<ApiResult<undefined, DeleteError>> {
		return await this._call("/tunnels/delete", req) as ApiResult<undefined, DeleteError>;
	}
	async tunnels_rename(req: ReqTunnelsRename): Promise<ApiResult<undefined, TunnelRenameError>> {
		return await this._call("/tunnels/rename", req) as ApiResult<undefined, TunnelRenameError>;
	}
	async tunnels_firewall_assign(req: ReqTunnelsFirewallAssign): Promise<ApiResult<undefined, TunnelsFirewallAssignError>> {
		return await this._call("/tunnels/firewall/assign", req) as ApiResult<undefined, TunnelsFirewallAssignError>;
	}
	async tunnels_ratelimit(req: ReqTunnelsRatelimit): Promise<ApiResult<undefined, TunnelRatelimitError>> {
		return await this._call("/tunnels/ratelimit", req) as ApiResult<undefined, TunnelRatelimitError>;
	}
	async tunnels_enable(req: ReqTunnelsEnable): Promise<ApiResult<undefined, TunnelEnableError>> {
		return await this._call("/tunnels/enable", req) as ApiResult<undefined, TunnelEnableError>;
	}
	async tunnels_proxy_set(req: ReqTunnelsProxySet): Promise<ApiResult<undefined, TunnelProxySetError>> {
		return await this._call("/tunnels/proxy/set", req) as ApiResult<undefined, TunnelProxySetError>;
	}
	async claim_setup(req: ReqClaimSetup): Promise<ApiResult<ClaimSetupResponse, ClaimSetupError>> {
		return await this._call("/claim/setup", req) as ApiResult<ClaimSetupResponse, ClaimSetupError>;
	}
	async claim_exchange(req: ReqClaimExchange): Promise<ApiResult<AgentSecretKey, ClaimExchangeError>> {
		return await this._call("/claim/exchange", req) as ApiResult<AgentSecretKey, ClaimExchangeError>;
	}
	async agents_rename(req: ReqAgentsRename): Promise<ApiResult<undefined, AgentRenameError>> {
		return await this._call("/agents/rename", req) as ApiResult<undefined, AgentRenameError>;
	}
	async agents_routing_set(req: ReqAgentsRoutingSet): Promise<ApiResult<undefined, AgentRoutingSetError>> {
		return await this._call("/agents/routing/set", req) as ApiResult<undefined, AgentRoutingSetError>;
	}
	async agents_routing_get(req: ReqAgentsRoutingGet): Promise<ApiResult<AgentRouting, AgentRoutingGetError>> {
		return await this._call("/agents/routing/get", req) as ApiResult<AgentRouting, AgentRoutingGetError>;
	}
	async agents_rundata(): Promise<ApiResultNoFail<AgentRunData>> {
		return await this._call("/agents/rundata", {}) as ApiResultNoFail<AgentRunData>;
	}
	async domains_list(): Promise<ApiResultNoFail<Domains>> {
		return await this._call("/domains/list", {}) as ApiResultNoFail<Domains>;
	}
	async shop_prices(): Promise<ApiResultNoFail<ShopPrices>> {
		return await this._call("/shop/prices", {}) as ApiResultNoFail<ShopPrices>;
	}
	async shop_availability_custom_domain(req: ReqShopAvailabilityCustomDomain): Promise<ApiResultNoFail<IsAvailable>> {
		return await this._call("/shop/availability/custom_domain", req) as ApiResultNoFail<IsAvailable>;
	}
	async proto_register(req: ReqProtoRegister): Promise<ApiResult<SignedAgentKey, ProtoRegisterError>> {
		return await this._call("/proto/register", req) as ApiResult<SignedAgentKey, ProtoRegisterError>;
	}
	async charge_get(req: ReqChargeGet): Promise<ApiResult<ChargeDetails, ChargeGetError>> {
		return await this._call("/charge/get", req) as ApiResult<ChargeDetails, ChargeGetError>;
	}
	async charge_refund(req: ReqChargeRefund): Promise<ApiResult<undefined, ChargeRefundError>> {
		return await this._call("/charge/refund", req) as ApiResult<undefined, ChargeRefundError>;
	}
	async query_region(req: ReqQueryRegion): Promise<ApiResult<QueryRegion, QueryRegionError>> {
		return await this._call("/query/region", req) as ApiResult<QueryRegion, QueryRegionError>;
	}
}
export default ApiBase;

export type ApiResponseError = { type: "validation", message: string }
	| { type: "path-not-found", message: PathNotFound }
	| { type: "auth", message: AuthError }
	| { type: "internal", message: ApiInternalError };

export type String = string;

export type PathNotFound = {
	path: String;
};

export type AuthError = "AuthRequired"
	| "InvalidHeader"
	| "InvalidSignature"
	| "InvalidTimestamp"
	| "InvalidApiKey"
	| "InvalidAgentKey"
	| "SessionExpired"
	| "InvalidAuthType"
	| "ScopeNotAllowed"
	| "NoLongerValid"
	| "GuestAccountNotAllowed"
	| "EmailMustBeVerified"
	| "AccountDoesNotExist"
	| "AdminOnly"
	| "InvalidToken"
	| "TotpRequred"
	| "NotAllowedWithReadOnly"
	| "DefaultAgentBlocked"
	| "AgentNotSelfManaged"
	| "SelfManagedAgentCanOnlyAffectSelf"
	| "AccountNotAuthorized";

export type ApiInternalError = {
	trace_id: String;
};

export type ApiResult<S, F> = { status: "success", data: S } | { status: "fail", data: F } | { status: "error", data: ApiResponseError };
export type ApiResultNoFail<S> = { status: "success", data: S } | { status: "error", data: ApiResponseError };
export type Option<T> = T | null;
export type Arc<T> = T;
export type Box<T> = T;
export type Vec<T> = T[];

export type ReqTunnelsListV1 = object;

export type AccountTunnelsV1 = {
	tunnels: Vec<AccountTunnelV1>;
};

export type AccountTunnelV1 = {
	id: Uuid;
	created_at: DateTimeUtc;
	name: Option<String>;
	user_enabled: bool;
	offline_reasons: Option<Vec<AccountTunnelOfflineReason>>;
	tunnel_type: Option<TunnelType>;
	port_type: PortType;
	port_count: u16;
	firewall_id: Option<Uuid>;
	props: AccountTunnelProps;
	origin: AccountTunnelOrigin;
	port_allocation_requests: Vec<PortAllocationRequest>;
	public_allocations: Vec<PublicAllocation>;
	connect_addresses: Vec<ConnectAddress>;
};

export type Uuid = string;

export type DateTimeUtc = string;

export type bool = boolean;

export type AccountTunnelOfflineReason = "OriginNotSet"
	| "AgentDisabled"
	| "AgentOverLimit"
	| "TunnelDisabled"
	| "PublicAllocationMissing"
	| "PublicAllocationPending";

export type TunnelType = "minecraft-java"
	| "minecraft-bedrock"
	| "valheim"
	| "terraria"
	| "starbound"
	| "rust"
	| "7days"
	| "unturned"
	| "https"
	| "hytale"
	| "project-zomboid"
	| "vintage-story";

export type PortType = "tcp"
	| "udp"
	| "both";

export type u16 = number;

export type AccountTunnelProps = {
	hostname_verify_level: HostnameVerifyLevel;
};

export type HostnameVerifyLevel = "None"
	| "NoRawIp"
	| "NoAutoName";

export type AccountTunnelOrigin = { type: "not-set", details: TunnelOriginNotSet }
	| { type: "agent", details: TunnelToAgent };

export type TunnelOriginNotSet = {
	agent_config: Option<HasAgentConfig>;
};

export type HasAgentConfig = {
	config_schema_id: Uuid;
	config_data: AgentTunnelConfig;
};

export type AgentTunnelConfig = {
	fields?: Vec<AgentTunnelAttr>;
};

export type AgentTunnelAttr = {
	name: String;
	value: String;
};

export type TunnelToAgent = {
	agent_id: Uuid;
	name: String;
	config_schema_id: Uuid;
	config_data: AgentTunnelConfig;
	config_invalid: Option<InvalidTunnelConfig>;
};

export type InvalidTunnelConfig = {
	agent_schema_id: Uuid;
	current_schema: AgentTunnelSchema;
	target_schema: Option<AgentTunnelSchema>;
};

export type AgentTunnelSchema = {
	fields: { [key in String]: AgentTunnelSchemaField };
};

export type AgentTunnelSchemaField = {
	label?: Option<String>;
	description?: Option<String>;
	value_type: AgentTunnelAttrType;
	allow_null: bool;
	default_value?: Option<String>;
	variants?: Option<Vec<String>>;
};

export type AgentTunnelAttrType = "Ip"
	| "Ip4"
	| "Ip6"
	| "SockAddr"
	| "SockAddr4"
	| "SockAddr6"
	| "Port"
	| "U64"
	| "I64"
	| "Boolean"
	| "String";

export type PortAllocationRequest = {
	id: Uuid;
	status: PortAllocationStatus;
	region: PlayitNetwork;
	public_port?: Option<u16>;
	public_ip?: Option<IpAddr>;
};

export type PortAllocationStatus = "Pending"
	| "RanOutOfPorts"
	| "PublicPortNotAvailable"
	| "NoPortsAvailableOnIp"
	| "AccountPortLimitReached"
	| string;

export type PlayitNetwork = "global"
	| "north-america"
	| "europe"
	| "asia"
	| "india"
	| "south-america"
	| "chile"
	| "seattle-washington"
	| "los-angeles-california"
	| "denver-colorado"
	| "dallas-texas"
	| "chicago-illinois"
	| "new-york"
	| "_NaReserved1"
	| "_NaReserved2"
	| "united-kingdom"
	| "germany"
	| "sweden"
	| "poland"
	| "romania"
	| "_Test"
	| "japan"
	| "australia";

export type IpAddr = string;

export type PublicAllocation = { type: "PortAllocation", details: PortAllocation }
	| { type: "Gateway", details: GatewayAllocation };

export type PortAllocation = {
	alloc_id: Uuid;
	ip_region: PlayitNetwork;
	ip_hostname: String;
	auto_domain: String;
	ip: IpAddr;
	port: u16;
	port_count: u16;
	port_type: PortType;
	expire_notice: Option<ExpireNotice>;
};

export type ExpireNotice = {
	disable_at: DateTimeUtc;
	remove_at: DateTimeUtc;
	reason: DisabledReason;
};

export type DisabledReason = "requires-premium"
	| "over-port-limit";

export type GatewayAllocation = {
	id: Option<Uuid>;
	hostname: String;
	region: PlayitNetwork;
};

export type ConnectAddress = { type: "addr4", value: ConnectAddr4 }
	| { type: "addr6", value: ConnectAddr6 }
	| { type: "ip4", value: ConnectIp4 }
	| { type: "ip6", value: ConnectIp6 }
	| { type: "auto", value: ConnectAutoName }
	| { type: "domain", value: ConnectDomain };

export type ConnectAddr4 = {
	address: SocketAddrV4;
	source: ConnectAddressSource;
};

export type SocketAddrV4 = string;

export type ConnectAddressSource = { resource: "port-allocation", id: Uuid }
	| { resource: "gateway", id: Uuid };

export type ConnectAddr6 = {
	address: SocketAddrV6;
	source: ConnectAddressSource;
};

export type SocketAddrV6 = string;

export type ConnectIp4 = {
	address: Ipv4Addr;
	default_port: u16;
	source: ConnectAddressSource;
};

export type Ipv4Addr = string;

export type ConnectIp6 = {
	address: Ipv6Addr;
	default_port: u16;
	source: ConnectAddressSource;
};

export type Ipv6Addr = string;

export type ConnectAutoName = {
	address: String;
	source: ConnectAddressSource;
};

export type ConnectDomain = {
	id: Uuid;
	domain: String;
	address: String;
	mode: DomainMode;
	source: ConnectAddressSource;
};

export type DomainMode = "Ip"
	| "Srv"
	| "SrvAndIp"
	| "Hostname";

export type Undefined = undefined;

export type ReqTunnelsCreateV1 = {
	name: String;
	protocol: TunnelProtocol;
	origin: AccountTunnelOriginCreate;
	endpoint: CreateTunnelEndpoint;
	enabled?: bool;
	firewall_id?: Option<Uuid>;
};

export type TunnelProtocol = { type: "tunnel-type", details: TunnelType }
	| { type: "raw-ports", details: TunnelProtocolRawPorts };

export type TunnelProtocolRawPorts = {
	port_type: PortType;
	port_count: u16;
	software_description: String;
};

export type AccountTunnelOriginCreate = { type: "agent", data: AgentOrigin };

export type AgentOrigin = {
	agent_id: Option<Uuid>;
	config?: AgentTunnelConfig;
};

export type CreateTunnelEndpoint = { type: "gateway", details: UseGateway }
	| { type: "dedicated-ip", details: UseAllocDedicatedIp }
	| { type: "shared-ip", details: UseAllocSharedIp }
	| { type: "region", details: UseAllocRegion }
	| { type: "port-allocation", details: Uuid };

export type UseGateway = {
	gateway_id: Uuid;
};

export type UseAllocDedicatedIp = {
	ip_hostname: String;
	port: Option<u16>;
};

export type UseAllocSharedIp = {
	ip_hostname: String;
	port: Option<u16>;
};

export type UseAllocRegion = {
	region: PlayitNetwork;
	port: Option<u16>;
};

export type ObjectId = {
	id: Uuid;
};

export type TunnelCreateErrorV1 = "AgentNotFound"
	| "InvalidAgentId"
	| "DedicatedIpNotFound"
	| "PortAllocNotFound"
	| "InvalidIpHostname"
	| "InvalidPortCount"
	| "RequiresVerifiedAccount"
	| "RegionNotSupported"
	| "InvalidTunnelConfig"
	| "FirewallNotFound"
	| "TunnelNameIsNotAscii"
	| "TunnelNameTooLong"
	| "PortAllocDoesNotMatchPortDetails"
	| "RegionRequiresPlayitPremium"
	| "PortAllocCurrentlyAssigned"
	| "PublicPortRequiresPlayitPremium"
	| "AgentVersionTooOld"
	| "RequiresPlayitPremium"
	| "EndpointDoesNotSupportProtocol"
	| "InvalidGatewayId"
	| "GatewayAlreadyHasTunnelType"
	| "GatewayDoesNotSupportTunnelType"
	| "TunnelTypeBlockedOnRegion"
	| "InvalidSoftwareDescription";

export type ReqSchemasGetV1 = {
	id: Uuid;
};

export type SchemaData = {
	id: Uuid;
	details: AgentSchema;
};

export type AgentSchema = {
	default_schema?: Option<AgentTunnelSchema>;
	schemas?: Vec<AgentSchemaForTunnelType>;
	only_explicit_schemas?: bool;
};

export type AgentSchemaForTunnelType = {
	tunnel_type: AgentSchemaTunnelType;
	schema?: Option<AgentTunnelSchema>;
};

export type AgentSchemaTunnelType = { name: "custom-tcp", details: AgentTunnelTypeSupportedPorts }
	| { name: "custom-udp", details: AgentTunnelTypeSupportedPorts }
	| { name: "custom-both", details: AgentTunnelTypeSupportedPorts }
	| { name: "tunnel-type", details: TunnelType };

export type AgentTunnelTypeSupportedPorts = {
	min: u16;
	max: u16;
};

export type SchemaGetError = "SchemaNotFound";

export type ReqTunnelsConfigV1 = {
	tunnel_id: Uuid;
	new_agent_id: Option<Uuid>;
	new_config: Option<AgentTunnelConfig>;
};

export type TunnelConfigError = { error: "TunnelNotFound", details: null }
	| { error: "AgentNotFound", details: null }
	| { error: "AgentVersionUnknown", details: null }
	| { error: "CannotConfigTunnelWithoutAgent", details: null }
	| { error: "SelfManagedAgentCannotReassignTunnel", details: null }
	| { error: "InvalidConfig", details: AgentSchemaValidationError }
	| { error: "ConfigNotCompatibleWithAgent", details: null }
	| { error: "NothingToUpdate", details: null };

export type AgentSchemaValidationError = { error: "NoSchemaFound", field: null }
	| { error: "TooManyFields", field: null }
	| { error: "TunnelTypeNotSupported", field: TunnelEndpointPortRequirements }
	| { error: "UnknownField", field: String }
	| { error: "MissingRequiredField", field: String }
	| { error: "InvalidValueForType", field: String }
	| { error: "ValueNotInVariants", field: String };

export type TunnelEndpointPortRequirements = {
	tunnel_type: Option<TunnelType>;
	port_type: PortType;
	port_count: u16;
};

export type ReqTunnelsPropset = {
	tunnel_id: Uuid;
	details: PropsetDetails;
};

export type PropsetDetails = { type: "hostname_verify_level", value: HostnameVerifyLevel }
	| { type: "custom_tunnel_details", value: String };

export type TunnelPropSetError = "RequiresPermium"
	| "TunnelNotFound"
	| "PropertyValueNotSupportedForTunnelType"
	| "PropertyValueInvalid";

export type ReqTunnelsTypeset = {
	tunnel_id: Uuid;
	tunnel_type: TunnelType;
};

export type TunnelTypeSetError = "RequiresPermium"
	| "TunnelNotFound"
	| "TunnelHasInvalidSettingsForType"
	| "CannotChangeTunnelType";

export type ReqAgentsRundataV1 = object;

export type AgentRunDataV1 = {
	agent_id: Uuid;
	tunnels: Vec<AgentTunnelV1>;
	pending: Vec<AgentPendingTunnelV1>;
	notices: Vec<AgentNotice>;
	permissions: AgentPermissions;
};

export type AgentTunnelV1 = {
	id: Uuid;
	internal_id: u64;
	name: String;
	display_address: String;
	port_type: PortType;
	port_count: u16;
	tunnel_type: Option<String>;
	tunnel_type_display: String;
	agent_config: AgentTunnelConfig;
	disabled_reason: Option<Cow<'static,str>>;
};

export type u64 = number;

export type AgentPendingTunnelV1 = {
	id: Uuid;
	name: String;
	tunnel_type: Option<String>;
	tunnel_type_display: String;
	port_type: PortType;
	port_count: u16;
	status_msg: String;
};

export type AgentNotice = {
	priority: AgentNoticePriority;
	message: Cow<'static,str>;
	resolve_link: Option<String>;
};

export type AgentNoticePriority = "Critical"
	| "High"
	| "Low";

export type AgentPermissions = {
	is_self_managed: bool;
	has_premium: bool;
	account_status: AccountStatus;
};

export type AccountStatus = "guest"
	| "email-not-verified"
	| "verified";

export type ReqInfoPops = object;

export type PlayitPops = {
	pops: Vec<Pop>;
	regions: Vec<PlayitNetwork>;
};

export type Pop = {
	pop: PlayitPop;
	name: String;
	region: PlayitNetwork;
	online: bool;
	ip4_premium: bool;
};

export type PlayitPop = "Any"
	| "USLosAngeles"
	| "USSeattle"
	| "USDallas"
	| "USMiami"
	| "USChicago"
	| "USNewJersey"
	| "CanadaToronto"
	| "Mexico"
	| "BrazilSaoPaulo"
	| "Spain"
	| "London"
	| "Germany"
	| "Poland"
	| "Sweden"
	| "IndiaDelhi"
	| "IndiaMumbai"
	| "IndiaBangalore"
	| "Singapore"
	| "Tokyo"
	| "Sydney"
	| "SantiagoChile"
	| "Israel"
	| "Romania"
	| "USNewYork"
	| "USDenver"
	| "Staging";

export type ReqLoginSignin = LoginCredentials;

export type LoginCredentials = {
	email: String;
	password: String;
};

export type WebSession = {
	session_key: String;
	auth: WebAuthToken;
};

export type WebAuthToken = {
	update_version: u32;
	account_id: u64;
	timestamp: u64;
	account_status: AccountStatus;
	totp_status: TotpStatus;
	admin_id: Option<u64>;
	admin_review_id: Option<u64>;
	read_only: bool;
	show_admin: bool;
};

export type u32 = number;

export type TotpStatus = { status: "required" }
	| { status: "not-setup" }
	| { status: "signed" } & SignedEpoch;

export type SignedEpoch = {
	epoch_sec: u32;
};

export type SigninFail = "IncorrectCredentials"
	| "AccountBanned";

export type ReqLoginClearcookie = object;

export type ClearWebSession = object;

export type ReqLoginCreateGuest = object;

export type LoginCreateGuestError = "Blocked";

export type ReqLoginGuest = object;

export type GuestLoginError = "AccountIsNotGuest";

export type ReqLoginResetPassword = {
	email: String;
	reset_code: String;
	new_password: String;
};

export type PasswordResetError = "ResetCodeExpired"
	| "InvalidResetCode"
	| "InvalidNewPassword";

export type ReqLoginResetSend = {
	email: String;
};

export type ReqTunnelsCreate = {
	name: Option<String>;
	tunnel_type: Option<TunnelType>;
	tunnel_description: Option<String>;
	port_type: PortType;
	port_count: u16;
	origin: TunnelOriginCreate;
	enabled: bool;
	alloc: Option<TunnelCreateUseAllocation>;
	firewall_id: Option<Uuid>;
	proxy_protocol: Option<ProxyProtocol>;
};

export type TunnelOriginCreate = { type: "default", data: AssignedDefaultCreate }
	| { type: "agent", data: AssignedAgentCreate }
	| { type: "managed", data: AssignedManagedCreate };

export type AssignedDefaultCreate = {
	local_ip: IpAddr;
	local_port: Option<u16>;
};

export type AssignedAgentCreate = {
	agent_id: Uuid;
	local_ip: IpAddr;
	local_port: Option<u16>;
};

export type AssignedManagedCreate = {
	agent_id: Option<Uuid>;
};

export type TunnelCreateUseAllocation = { type: "dedicated-ip", details: UseAllocDedicatedIp }
	| { type: "port-allocation", details: UseAllocPortAlloc }
	| { type: "region", details: UseRegion };

export type UseAllocPortAlloc = {
	alloc_id: Uuid;
};

export type UseRegion = {
	region: PlayitNetwork;
};

export type ProxyProtocol = "proxy-protocol-v1"
	| "proxy-protocol-v2";

export type TunnelCreateError = "DefaultAgentNotSupported"
	| "AgentNotFound"
	| "InvalidAgentId"
	| "AgentVersionTooOld"
	| "DedicatedIpNotFound"
	| "DedicatedIpPortNotAvailable"
	| "DedicatedIpNotEnoughSpace"
	| "PortAllocNotFound"
	| "InvalidIpHostname"
	| "ManagedMissingAgentId"
	| "InvalidPortCount"
	| "RequiresVerifiedAccount"
	| "InvalidTunnelName"
	| "FirewallNotFound"
	| "AllocInvalid"
	| "InvalidOrigin"
	| "RequiresPlayitPremium"
	| "TunnelTypeBlockedOnRegion"
	| "TunnelTypeRequiresDescription"
	| "Other";

export type ReqTunnelsList = {
	tunnel_id: Option<Uuid>;
	agent_id: Option<Uuid>;
};

export type AccountTunnels = {
	tunnels: Vec<AccountTunnel>;
	tcp_alloc: AllocatedPorts;
	udp_alloc: AllocatedPorts;
};

export type AccountTunnel = {
	id: Uuid;
	tunnel_type: Option<TunnelType>;
	created_at: DateTimeUtc;
	name: Option<String>;
	port_type: PortType;
	port_count: u16;
	alloc: AccountTunnelAllocation;
	origin: Option<TunnelOrigin>;
	domain: Option<TunnelDomain>;
	firewall_id: Option<Uuid>;
	ratelimit: Ratelimit;
	active: bool;
	disabled_reason: Option<TunnelOfflineReason>;
	region: Option<PlayitNetwork>;
	expire_notice: Option<ExpireNotice>;
	proxy_protocol: Option<ProxyProtocol>;
	hostname_verify_level: HostnameVerifyLevel;
	agent_over_limit: bool;
};

export type AccountTunnelAllocation = { status: "pending", data: null }
	| { status: "disabled", data: TunnelDisabled }
	| { status: "allocated", data: TunnelAllocated };

export type TunnelDisabled = {
	reason: TunnelOfflineReason;
};

export type TunnelOfflineReason = "requires-premium"
	| "over-port-limit"
	| "ip-used-in-gre"
	| "public-port-not-available";

export type TunnelAllocated = {
	id: Uuid;
	ip_hostname: String;
	static_ip4: Option<Ipv4Addr>;
	static_ip6: Ipv6Addr;
	assigned_domain: String;
	assigned_srv: Option<String>;
	tunnel_ip: IpAddr;
	port_start: u16;
	port_end: u16;
	assignment: TunnelAssignment;
	ip_type: IpType;
	region: PlayitNetwork;
};

export type TunnelAssignment = { type: "dedicated-ip", subscription: TunnelDedicatedIp }
	| { type: "shared-ip", subscription: null }
	| { type: "dedicated-port", subscription: SubscriptionId };

export type TunnelDedicatedIp = {
	sub_id: Uuid;
	region: PlayitNetwork;
};

export type SubscriptionId = {
	sub_id: Uuid;
};

export type IpType = "both"
	| "ip4"
	| "ip6";

export type TunnelOrigin = { type: "agent", data: AssignedAgent }
	| { type: "managed", data: AssignedManaged };

export type AssignedAgent = {
	agent_id: Uuid;
	agent_name: String;
	local_ip: IpAddr;
	local_port: Option<u16>;
};

export type AssignedManaged = {
	agent_id: Uuid;
	agent_name: String;
};

export type TunnelDomain = {
	id: Uuid;
	name: String;
};

export type Ratelimit = {
	bytes_per_second: Option<u32>;
	packets_per_second: Option<u32>;
};

export type AllocatedPorts = {
	allowed: u32;
	claimed: u32;
	desired: u32;
};

export type ReqTunnelsUpdate = {
	tunnel_id: Uuid;
	local_ip: IpAddr;
	local_port: Option<u16>;
	agent_id: Option<Uuid>;
	enabled: bool;
};

export type UpdateError = "ChangingAgentIdNotAllowed"
	| "TunnelNotFound"
	| "CannotUpdateLocalAddressForUnassignedTunnel"
	| "InvalidAgentId"
	| "AddressOrProxyProtoNotSupportedByAgent";

export type ReqTunnelsDelete = {
	tunnel_id: Uuid;
};

export type DeleteError = "TunnelNotFound";

export type ReqTunnelsRename = {
	tunnel_id: Uuid;
	name: String;
};

export type TunnelRenameError = "TunnelNotFound"
	| "NameTooLong";

export type ReqTunnelsFirewallAssign = {
	tunnel_id: Uuid;
	firewall_id: Option<Uuid>;
};

export type TunnelsFirewallAssignError = "TunnelNotFound"
	| "InvalidFirewallId";

export type ReqTunnelsRatelimit = {
	tunnel_id: Uuid;
	bytes_per_second: Option<u32>;
	packets_per_second: Option<u32>;
};

export type TunnelRatelimitError = "TunnelNotFound"
	| "InvalidRatelimit"
	| "PlayitPremiumRequired";

export type ReqTunnelsEnable = {
	tunnel_id: Uuid;
	enabled: bool;
};

export type TunnelEnableError = "TunnelNotFound";

export type ReqTunnelsProxySet = {
	tunnel_id: Uuid;
	proxy_protocol: Option<ProxyProtocol>;
};

export type TunnelProxySetError = "TunnelNotFound"
	| "ProxyProtocolNotSupportedByAgent";

export type ReqClaimSetup = {
	code: String;
	agent_type: ClaimAgentType;
	version: String;
};

export type ClaimAgentType = "assignable"
	| "self-managed";

export type ClaimSetupResponse = "WaitingForUserVisit"
	| "WaitingForUser"
	| "UserAccepted"
	| "UserRejected";

export type ClaimSetupError = "InvalidCode"
	| "CodeExpired"
	| "VersionTextTooLong";

export type ReqClaimExchange = {
	code: String;
};

export type AgentSecretKey = {
	secret_key: String;
};

export type ClaimExchangeError = "CodeNotFound"
	| "CodeExpired"
	| "UserRejected"
	| "NotAccepted"
	| "NotSetup";

export type ReqAgentsRename = {
	agent_id: Uuid;
	name: String;
};

export type AgentRenameError = "AgentNotFound"
	| "InvalidName"
	| "InvalidAgentId";

export type ReqAgentsRoutingSet = {
	agent_id: Uuid;
	routing: AgentRoutingTarget;
	disable_ip6?: bool;
};

export type AgentRoutingTarget = { type: "Automatic", details: null }
	| { type: "Pop", details: PlayitPop }
	| { type: "Region", details: PlayitNetwork };

export type AgentRoutingSetError = "RequiresPremium"
	| "AgentNotFound"
	| "InvalidAgentId";

export type ReqAgentsRoutingGet = {
	agent_id: Option<Uuid>;
};

export type AgentRouting = {
	agent_id: Uuid;
	targets4: Vec<Ipv4Addr>;
	targets6: Vec<Ipv6Addr>;
	disable_ip6: bool;
};

export type AgentRoutingGetError = "MissingAgentId"
	| "InvalidAgentId";

export type ReqAgentsRundata = object;

export type AgentRunData = {
	agent_id: Uuid;
	agent_type: AgentType;
	account_status: AgentAccountStatus;
	tunnels: Vec<AgentTunnel>;
	pending: Vec<AgentPendingTunnel>;
	account_features: AccountFeatures;
};

export type AgentType = "default"
	| "assignable"
	| "self-managed";

export type AgentAccountStatus = "account-delete-scheduled"
	| "banned"
	| "has-message"
	| "email-not-verified"
	| "guest"
	| "ready"
	| "agent-over-limit"
	| "agent-disabled";

export type AgentTunnel = {
	id: Uuid;
	internal_id: u64;
	name: Option<String>;
	ip_num: u64;
	region_num: u16;
	port: PortRange;
	proto: PortType;
	local_ip: IpAddr;
	local_port: u16;
	tunnel_type: Option<String>;
	assigned_domain: String;
	custom_domain: Option<String>;
	disabled: Option<AgentTunnelDisabled>;
	proxy_protocol: Option<ProxyProtocol>;
	agent_config: AgentTunnelConfig;
};

export type PortRange = {
	from: u16;
	to: u16;
};

export type AgentTunnelDisabled = "ByUser"
	| "BySystem";

export type AgentPendingTunnel = {
	id: Uuid;
	name: Option<String>;
	proto: PortType;
	port_count: u16;
	tunnel_type: Option<String>;
	is_disabled: bool;
	region_num: u16;
};

export type AccountFeatures = {
	regional_tunnels: bool;
};

export type ReqDomainsList = object;

export type Domains = {
	domains: Vec<Domain>;
};

export type Domain = {
	id: Uuid;
	name: String;
	is_external: bool;
	parent: Option<Uuid>;
	sub_id: Uuid;
	target: Option<DomainTarget>;
};

export type DomainTarget = { type: "ip-address", details: DomainTargetIp }
	| { type: "tunnel", details: DomainTargetTunnel }
	| { type: "external-cname", details: DomainTargetExternalCName }
	| { type: "gateway", details: DomainTargetGateway };

export type DomainTargetIp = {
	ip_address: IpAddr;
};

export type DomainTargetTunnel = {
	tunnel_id: Uuid;
};

export type DomainTargetExternalCName = {
	cname: String;
};

export type DomainTargetGateway = {
	gateway_id: Uuid;
};

export type ReqShopPrices = object;

export type ShopPrices = {
	custom_domain: ShopPrice;
	dedicated_ip: { [key in PlayitNetwork]: ShopPrice };
	playit_premium: ShopPrice;
	ports_both: ShopPrice;
	ports_tcp: ShopPrice;
	ports_udp: ShopPrice;
	dedicated_port_global: ShopPrice;
	dedicated_port_regional: ShopPrice;
};

export type ShopPrice = {
	monthly: Option<u32>;
	yearly: Option<u32>;
};

export type ReqShopAvailabilityCustomDomain = {
	name: String;
};

export type IsAvailable = {
	is_available: bool;
};

export type ReqProtoRegister = {
	agent_version?: Option<PlayitAgentVersion>;
	proto_version?: u64;
	version?: AgentVersion;
	platform?: Platform;
	client_addr: SocketAddr;
	tunnel_addr: SocketAddr;
};

export type PlayitAgentVersion = {
	version: AgentVersionOld;
	proto_version?: u64;
};

export type AgentVersionOld = {
	platform: Platform;
	version: String;
	has_expired?: bool;
};

export type Platform = "linux"
	| "freebsd"
	| "windows"
	| "macos"
	| "android"
	| "ios"
	| "docker"
	| "minecraft-plugin"
	| "unknown";

export type AgentVersion = {
	variant_id: Uuid;
	version_major: u32;
	version_minor: u32;
	version_patch: u32;
};

export type SocketAddr = string;

export type SignedAgentKey = {
	key: String;
};

export type ProtoRegisterError = "UnknownPlayitVersion"
	| "DisabledByUser"
	| "AgentDisabledOverLimit"
	| "AccountBanned";

export type ReqChargeGet = {
	reference_code: String;
};

export type ChargeDetails = {
	reference_code: String;
	created_at: DateTimeUtc;
	invoice_type: InvoiceType;
	invoice_status: InvoiceStatus;
	total_cost: String;
	items: Vec<ChargeDetailsItem>;
	refund: Option<RefundStatus>;
};

export type InvoiceType = "Subscription"
	| "StartSubscription"
	| "StripeSubscription";

export type InvoiceStatus = "draft"
	| "open"
	| "paid"
	| "void"
	| "uncollectible";

export type ChargeDetailsItem = {
	product: SubProductType;
	months: u32;
	total_cost: String;
};

export type SubProductType = "playit-premium"
	| "playit-premium-trial"
	| "dedicated-ip"
	| "udp-ports"
	| "tcp-ports"
	| "both-ports"
	| "custom-domain"
	| "dedicated-port-alloc";

export type RefundStatus = { type: "Pending", details: PendingRefundRequest }
	| { type: "Applied", details: RefundApplied }
	| { type: "DisputeCreated", details: DisputeCreated };

export type PendingRefundRequest = {
	created_at: DateTimeUtc;
	reason: RefundRequestReason;
};

export type RefundRequestReason = "fraud"
	| "not-satisfied"
	| "issuer-fraud-warning";

export type RefundApplied = {
	created_at: DateTimeUtc;
	refund_amount: String;
};

export type DisputeCreated = {
	created_at: DateTimeUtc;
};

export type ChargeGetError = "ChargeNotFound";

export type ReqChargeRefund = {
	reference_code: String;
	reason: RefundRequestReason;
	email: Option<String>;
	refund_message: Option<String>;
};

export type ChargeRefundError = "ChargeNotFound"
	| "MessageTooLarge"
	| "UnauthorizedReason";

export type ReqQueryRegion = {
	limit_region: Option<PlayitNetwork>;
};

export type QueryRegion = {
	region: PlayitNetwork;
	pop: PlayitPop;
};

export type QueryRegionError = "FailedToDetermineLocation";

