drop table wsrm_sender;
create table wsrm_sender (
	message_id varchar(255) not null,
	message_context_ref_key varchar(255),
	internal_sequence_id varchar(255),
	sequence_id varchar(255),
	to_address varchar(255),
	inbound_sequence_id varchar(255),
	send smallint,
	sent_count integer,
	message_number bigint,
	resend smallint,
	time_to_send bigint,
	message_type integer,
	last_message smallint,
	inbound_message_number bigint,
	transport_available smallint,
	flags integer,
	primary key (message_id)
);
	
drop table wsrm_rmd;
create table wsrm_rmd (
	sequence_id varchar(255) not null,
	to_epr_addr varchar(255),
	to_epr blob,
	reply_to_epr_addr varchar(255),
	reply_to_epr blob,
	acks_to_epr_addr varchar(255),
	acks_to_epr blob,
	rm_version varchar(255),
	security_token_data varchar(255),
	last_activated_time bigint,
	closed smallint,
	terminated_flag smallint,
	polling_mode smallint,
	service_name varchar(255),
	flags integer,
	reference_message_key varchar(255),
	highest_in_message_id varchar(255),
	last_in_message_id varchar(255),
	server_completed_messages clob,
	outof_order_ranges clob,
	to_address varchar(255),
	outbound_internal_sequence varchar(255),
	next_msgno_to_process bigint,
	highest_in_message_number bigint,
	rmd_flags integer,
	primary key (sequence_id)
);
	
drop table wsrm_rms;
create table wsrm_rms (
	create_seq_msg_id varchar(255) not null,
	sequence_id varchar(255),
	to_epr_addr varchar(255),
	to_epr blob,
	reply_to_epr_addr varchar(255),
	reply_to_epr blob,
	acks_to_epr_addr varchar(255),
	acks_to_epr blob,
	rm_version varchar(255),
	security_token_data varchar(255),
	last_activated_time BIGINT,
	closed smallint,
	terminated_flag smallint,
	polling_mode smallint,
	service_name varchar(255),
	flags integer,
	id bigint,
	internal_sequence_id varchar(255),
	create_sequence_msg_store_key varchar(255),
	reference_msg_store_key varchar(255),
	last_send_error blob,
	highest_out_relates_to varchar(255),
	client_completed_messages clob,
	transport_to varchar(255),
	offered_endpoint varchar(255),
	offered_endpoint_epr_addr varchar(255),
	offered_endpoint_epr blob,
	offered_sequence varchar(255),
	anonymous_uuid varchar(255),
	last_send_error_timestamp bigint,
	last_out_message bigint,
	highest_out_message_number bigint,
	next_message_number bigint,
	terminate_added smallint,
	timed_out smallint,
	sequence_closed_client smallint,
	expected_replies bigint,
	soap_version integer,
	termination_pauser_for_cs smallint,
	avoid_auto_termination smallint,
	rms_flags integer,
	reallocated smallint,
	internalSeqIDOfSeqUsedForReallocation varchar(255),
	primary key (create_seq_msg_id)
);
	
drop table wsrm_invoker;	
create table wsrm_invoker (
	message_context_ref_key varchar(255) not null,
	sequence_id varchar(255),
	context blob,
	msg_no bigint,
	flags integer,
	PRIMARY KEY (message_context_ref_key)
);

drop table wsrm_msgctx;	
create table wsrm_msgctx (
	ctx_key varchar(255) not null,
	ctx blob,
	PRIMARY KEY (ctx_key)
);

