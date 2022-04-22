insert into BAKEOFF values (1,'2022-03-30','Cheesecake');

insert into BAKER values (1,'Hannah Dillow');
insert into BAKER values (2,'Scotty too hotty');
insert into BAKER values (3,'Jamie Freeman');
insert into BAKER values (4,'James Tipping');
insert into BAKER values (5,'Ellen Venn');
insert into BAKER values (6,'Teddy Bear');
insert into BAKER values (7,'Brad Smith');
insert into BAKER values (8,'Harry Osbourn');
insert into BAKER values (9,'Callum Nightingale');
insert into BAKER values (10,'Zaaaach Sproston');
insert into BAKER values (11,'Mark Beresford');

insert into JUDGE values (1,'Bella');
insert into JUDGE values (2,'Paul A');

insert into JUDGE_HISTORY values (1, 1, 1);
insert into JUDGE_HISTORY values (2, 2, 1);

insert into PARTICIPANT values (1,1,1,1, 'Hippo', '9a5440cb-0d65-4bb0-95b1-8beb8cb7758e');
insert into PARTICIPANT values (2,2,1,2, 'Lemon', NULL);
insert into PARTICIPANT values (3,3,1,3, 'Chocolate Orange', 'eda04c5a-3e38-4873-90de-a6057b1f3608');
insert into PARTICIPANT values (4,4,1,4, 'Bueno', '4f6f8e7b-a720-4e1a-93fd-47127019269a');
insert into PARTICIPANT values (5,5,1,5, 'Biscoff', '0e4bc5b6-459b-42b3-b843-fc4db5c5459a);
insert into PARTICIPANT values (6,6,1,6, 'Chocolate Orange', '6a0bf40f-365f-4b6e-916e-a8b567a39874');
insert into PARTICIPANT values (7,7,1,7, 'Mini Egg', NULL);
insert into PARTICIPANT values (8,8,1,8, 'Triple Layered Cookie Dough', 'c0471e36-ea4d-4053-ab4d-b74e840ad4db');
insert into PARTICIPANT values (9,9,1,9, 'Lemon and Raspberry', NULL);
insert into PARTICIPANT values (10,10,1,10, 'Baked Creme Egg', '17be4562-fc80-4ca9-9a02-bb85af58709d');
insert into PARTICIPANT values (11,11,1,11, 'Surprise', NULL);

insert into RESULT values (1, 1, 1, 0, 7);
insert into RESULT values (2, 1, 2, 0, 8);
insert into RESULT values (3, 2, 1, 0, 5);
insert into RESULT values (4, 2, 2, 0, 8);
insert into RESULT values (5, 3, 1, 0, 6);
insert into RESULT values (6, 3, 2, 0, 9);
insert into RESULT values (7, 4, 1, 0, 8);
insert into RESULT values (8, 4, 2, 0, 8);
insert into RESULT values (9, 5, 1, 0, 7);
insert into RESULT values (10, 5, 2, 0, 9);
insert into RESULT values (11, 6, 1, 0, 5);
insert into RESULT values (12, 6, 2, 0, 8);
insert into RESULT values (13, 7, 1, 0, 5);
insert into RESULT values (14, 7, 2, 0, 7);
insert into RESULT values (15, 8, 1, 0, 8);
insert into RESULT values (16, 8, 2, 0, 7);
insert into RESULT values (17, 9, 1, 0, 7);
insert into RESULT values (18, 9, 2, 0, 9);
insert into RESULT values (19, 10, 1, 0, 8);
insert into RESULT values (20, 10, 2, 0, 8);
insert into RESULT values (21, 11, 1, 0, 7);
insert into RESULT values (22, 11, 2, 0, 7);

insert into BAKEOFF values (2,'2022-04-13','Hot Cross Buns');

insert into JUDGE values (3,'Harry');

insert into JUDGE_HISTORY values (3, 2, 2);
insert into JUDGE_HISTORY values (4, 3, 2);

insert into BAKER values (12,'Jon Vince');
insert into BAKER values (13,'Bella');
insert into BAKER values (14,'Adam Jones');

insert into PARTICIPANT values (12,5,2,1, 'Hot Cross Muffins', NULL);
insert into PARTICIPANT values (13,5,2,2, 'The OGs', 'cd29f0df-1089-4d97-92e5-0f149888a218');
insert into PARTICIPANT values (14,12,2,3, 'CST Bad Boys', 'a6d4404b-cf3e-445f-b0cc-bb5dd681637d');
insert into PARTICIPANT values (15,14,2,4, 'Hot Crossy Buns', '51ecc983-733c-4506-876a-7b4452f68067');
insert into PARTICIPANT values (16,13,2,5, 'Goats Cheese', '4723ba9d-c51f-40f8-85f4-eaf3e06b8b41');

insert into RESULT values (23, 12, 2, 6, 7);
insert into RESULT values (24, 12, 3, 5, 5.5);
insert into RESULT values (25, 13, 2, 9, 6);
insert into RESULT values (26, 13, 3, 10, 7);
insert into RESULT values (27, 14, 2, 9, 7);
insert into RESULT values (28, 14, 3, 9, 7);
insert into RESULT values (29, 15, 2, 5, 8);
insert into RESULT values (30, 15, 3, 4, 8);
insert into RESULT values (31, 16, 2, 6, 7);
insert into RESULT values (32, 16, 3, 5, 9.5);

commit;