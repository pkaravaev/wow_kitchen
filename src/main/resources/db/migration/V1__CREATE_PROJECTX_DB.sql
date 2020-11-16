--
-- Name: sms_auth; Type: TABLE; Schema: public; Owner: prog11
--

CREATE TABLE public.sms_auth (
                                 mobile_number character varying(200) NOT NULL,
                                 send_time timestamp without time zone NOT NULL,
                                 country_code character varying(10) DEFAULT ''::character varying,
                                 id integer NOT NULL,
                                 sms_code character varying(4) DEFAULT ''::character varying NOT NULL,
                                 used boolean DEFAULT false
);




--
-- Name: sms_auth_id_seq; Type: SEQUENCE; Schema: public; Owner: prog11
--

CREATE SEQUENCE public.sms_auth_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: sms_auth_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: prog11
--

ALTER SEQUENCE public.sms_auth_id_seq OWNED BY public.sms_auth.id;


--
-- Name: user; Type: TABLE; Schema: public; Owner: prog11
--

CREATE TABLE public."user" (
                               id integer NOT NULL,
                               name character varying(20) DEFAULT ''::character varying,
                               email character varying(30),
                               enabled boolean DEFAULT true,
                               address character varying(100) DEFAULT ''::character varying,
                               country_code character varying(10) DEFAULT ''::character varying,
                               mobile_number character varying(200) DEFAULT ''::character varying NOT NULL
);




--
-- Name: user_id_seq; Type: SEQUENCE; Schema: public; Owner: prog11
--

CREATE SEQUENCE public.user_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;



--
-- Name: user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: prog11
--

ALTER SEQUENCE public.user_id_seq OWNED BY public."user".id;


--
-- Name: user_roles; Type: TABLE; Schema: public; Owner: prog11
--

CREATE TABLE public.user_roles (
                                   user_id integer NOT NULL,
                                   role character varying(20) DEFAULT 'ROLE_USER'::character varying NOT NULL
);


--
-- Name: sms_auth id; Type: DEFAULT; Schema: public; Owner: prog11
--

ALTER TABLE ONLY public.sms_auth ALTER COLUMN id SET DEFAULT nextval('public.sms_auth_id_seq'::regclass);


--
-- Name: user id; Type: DEFAULT; Schema: public; Owner: prog11
--

ALTER TABLE ONLY public."user" ALTER COLUMN id SET DEFAULT nextval('public.user_id_seq'::regclass);


--
-- Data for Name: sms_auth; Type: TABLE DATA; Schema: public; Owner: prog11
--

INSERT INTO public.tb_sms_auth (mobile_number, send_time, country_code, id, sms_code, used) VALUES ('9637671950', '2019-06-18 13:57:41.185253', '7', 9, '6832', true);


--
-- Data for Name: user; Type: TABLE DATA; Schema: public; Owner: prog11
--

INSERT INTO public.tb_user (id, name, email, enabled, address, country_code, mobile_number) VALUES (13, NULL, NULL, true, NULL, '7', '9637671951');
INSERT INTO public.tb_user (id, name, email, enabled, address, country_code, mobile_number) VALUES (14, NULL, NULL, true, NULL, '7', '9637671952');


--
-- Data for Name: user_roles; Type: TABLE DATA; Schema: public; Owner: prog11
--

INSERT INTO public.tb_user_roles (user_id, role) VALUES (13, 'ROLE_USER');
INSERT INTO public.tb_user_roles (user_id, role) VALUES (14, 'ROLE_USER');


--
-- Name: sms_auth_id_seq; Type: SEQUENCE SET; Schema: public; Owner: prog11
--

SELECT pg_catalog.setval('public.sms_auth_id_seq', 9, true);


--
-- Name: user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: prog11
--

SELECT pg_catalog.setval('public.user_id_seq', 14, true);


--
-- Name: sms_auth sms_auth_pk; Type: CONSTRAINT; Schema: public; Owner: prog11
--

ALTER TABLE ONLY public.sms_auth
    ADD CONSTRAINT sms_auth_pk PRIMARY KEY (id);


--
-- Name: sms_auth sms_auth_pk_2; Type: CONSTRAINT; Schema: public; Owner: prog11
--

ALTER TABLE ONLY public.sms_auth
    ADD CONSTRAINT sms_auth_pk_2 UNIQUE (mobile_number, country_code);


--
-- Name: user user_pk; Type: CONSTRAINT; Schema: public; Owner: prog11
--

ALTER TABLE ONLY public."user"
    ADD CONSTRAINT user_pk PRIMARY KEY (id);


--
-- Name: user user_pk_2; Type: CONSTRAINT; Schema: public; Owner: prog11
--

ALTER TABLE ONLY public."user"
    ADD CONSTRAINT user_pk_2 UNIQUE (mobile_number, country_code);


--
-- Name: user_roles user_roles_user__fk; Type: FK CONSTRAINT; Schema: public; Owner: prog11
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_user__fk FOREIGN KEY (user_id) REFERENCES public."user"(id) ON DELETE CASCADE;


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: postgres
--

GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

