create database biblioteca;
use biblioteca;


create table user (
	ID integer unsigned not null primary key auto_increment,
    username varchar(25) not null,
    passworduser varchar(250) not null,
    status boolean not null default true,
    name varchar(25) not null,
    surname varchar(25) not null,
    registration_date date,
    email varchar(255) not null,
    constraint utente_unico unique(name,surname),
    constraint email_unica unique(email)
    );
    
create table transcription_project(
	ID integer unsigned not null primary key auto_increment,
    date date,
    ID_coordinator integer unsigned not null,
    constraint transcription_project foreign key(ID_coordinator) references user(ID) on update cascade on delete restrict
);

create table scanning_project(
	ID integer unsigned not null primary key auto_increment,
    date date,
    ID_coordinator integer unsigned not null,
    constraint scanning_project foreign key(ID_coordinator) references user(ID) on update cascade on delete restrict
);

create table document(
	ID integer unsigned not null primary key auto_increment,
    title varchar(70),
    scanning_complete boolean default false,
    transcription_complete boolean default false,
    ID_transcription_project integer unsigned not null,
    ID_scanning_project integer unsigned not null,
    constraint document_transcription_project foreign key(ID_transcription_project) references transcription_project(ID) on update cascade on delete restrict,
    constraint document_scanning_project foreign key(ID_scanning_project) references scanning_project(ID) on update cascade on delete restrict
);
    
create table page(
	ID integer unsigned not null primary key auto_increment,
    number integer unsigned,
    image varchar(255),
    transcription text,
    image_convalidation boolean default null,
    transcription_convalidation boolean default null,
    ID_transcription_reviser integer unsigned,
    ID_scanning_reviser integer unsigned,
    ID_digitalizer integer unsigned,
    ID_document integer unsigned not null,
    constraint page_transcription_reviser_user foreign key (ID_transcription_reviser) references user(ID) on update cascade on delete restrict,
    constraint page_scanning_reviser_user foreign key(ID_scanning_reviser) references user(ID) on update cascade on delete restrict,
    constraint page_ditigalizer_user foreign key(ID_digitalizer) references user(ID) on update cascade on delete restrict,
	constraint page_document foreign key(ID_document) references page(ID) on update cascade on delete restrict
);

create table document_metadata(
	ID integer unsigned not null primary key auto_increment,
    author varchar(30),
    description text,
    composition_date date,
    composition_period_from date,
    composition_period_to date,
    preservation_state enum('1','2','3','4','5'),
    #tags text,
    ID_document integer unsigned not null,
    constraint metadata_document foreign key(ID_document) references document(ID) on update cascade on delete restrict
);

create table tag(
	ID integer unsigned not null primary key auto_increment,
    name varchar(30) not null unique
);

create table tag_metadata(
	ID integer unsigned not null primary key auto_increment,
    ID_document_metadada integer unsigned not null,
    ID_tag integer unsigned,
    constraint tag_metadata_document_metadata foreign key (ID_document_metadada) references document_metadata(ID) on update cascade on delete cascade,
    constraint tag_metadata_tag foreign key(ID_tag) references tag(ID) on update cascade on delete cascade
);

create table book_marks(
	ID integer unsigned not null primary key auto_increment,
    ID_user integer unsigned not null,
    ID_page integer unsigned not null,
    constraint book_marks_user foreign key (ID_user) references user(ID) on update cascade on delete cascade,
    constraint book_marks_page foreign key(Id_page) references page(ID) on update cascade on delete cascade
);

create table personal_collection(
	ID integer unsigned not null primary key auto_increment,
    ID_user integer unsigned not null,
    ID_document integer unsigned not null,
    constraint personal_collection_user foreign key(ID_user) references user(ID) on update cascade on delete cascade,
    constraint personal_Collection_document foreign key(Id_document) references document(ID) on update cascade on delete cascade
);

create table permission(
	ID integer unsigned not null primary key auto_increment,
    name varchar(50) not null unique
);

create table document_collection(
	ID integer unsigned not null primary key auto_increment,
    name varchar(50) not null unique
);

create table transcription_assigned(
	ID integer unsigned not null primary key auto_increment,
    ID_transcriber_user integer unsigned ,
    ID_page integer unsigned not null,
    constraint transcription_assigned_transcriber_user foreign key(ID_transcriber_user) references user(ID) on update cascade,
    constraint transcription_assigned_page foreign key(ID_page) references page(ID) on update cascade on delete restrict
);

create table transcription_project_partecipant (
    ID integer unsigned not null primary key auto_increment,
    ID_transcription_project integer unsigned not null,
    ID_transcriber_user integer unsigned not null,
    constraint transcription_project_partecipant_transcription_project foreign key (ID_transcription_project) references transcription_project (ID) on update cascade on delete restrict,
    constraint transcription_project_partecipant_transcriber_user foreign key (ID_transcriber_user) references  user(ID) on update cascade on delete restrict 
);

create table scanning_project_partecipant(
	ID integer unsigned not null primary key auto_increment,
    ID_scanning_project integer unsigned not null,
    ID_digitalizer_user integer unsigned not null,
	constraint scanning_project_partecipant_scanning_project foreign key(ID_scanning_project) references scanning_project(ID) on update cascade on delete restrict,
    constraint scanning_project_partecipant_digitalizer_user foreign key(ID_digitalizer_user) references user(ID) on update cascade on delete restrict
);

create table perm_authorization(
	ID integer unsigned not null primary key auto_increment,
    ID_user integer unsigned not null,
	ID_permission integer unsigned ,
	constraint perm_authorization_user foreign key(ID_user) references user(ID) on update cascade on delete restrict,
    constraint perm_authorization_permission foreign key(ID_permission) references permission(ID) on update cascade on delete set null
);


