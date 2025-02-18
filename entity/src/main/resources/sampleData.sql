INSERT INTO public."user" (id, last_modified_at, created_at, last_login_at, name) VALUES ('system', NOW(), NOW(), null, 'system');

INSERT INTO public.type (id, parent) VALUES ('type_1', null);
INSERT INTO public.type (id, parent) VALUES ('type_2', 'type_1');
INSERT INTO public.type (id, parent) VALUES ('type_3', 'type_2');

INSERT INTO public.type_version (type, version, created_at, effective_at, expire_at, created_by) VALUES ('type_1', 't1-v1', '2025-02-16 18:13:23.066000 +00:00', '1970-01-01 00:00:00.000000 +00:00', '2999-12-31 00:00:00.000000 +00:00', 'system');
INSERT INTO public.type_version (type, version, created_at, effective_at, expire_at, created_by) VALUES ('type_2', 't2-v1', '2025-02-16 18:13:23.066000 +00:00', '1970-01-01 00:00:00.000000 +00:00', '1999-12-31 00:00:00.000000 +00:00', 'system');
INSERT INTO public.type_version (type, version, created_at, effective_at, expire_at, created_by) VALUES ('type_2', 't2-v2', '2025-02-16 18:13:23.066000 +00:00', '1999-12-31 00:00:00.000000 +00:00', '2999-12-31 00:00:00.000000 +00:00', 'system');
INSERT INTO public.type_version (type, version, created_at, effective_at, expire_at, created_by) VALUES ('type_3', 't3-v1', '2025-02-16 18:13:23.066000 +00:00', '1970-01-01 00:00:00.000000 +00:00', '2005-12-31 00:00:00.000000 +00:00', 'system');
INSERT INTO public.type_version (type, version, created_at, effective_at, expire_at, created_by) VALUES ('type_3', 't3-v2', '2025-02-16 18:13:23.066000 +00:00', '2005-12-31 00:00:00.000000 +00:00', '2999-12-31 00:00:00.000000 +00:00', 'system');

INSERT INTO public.type_definition (id, created_at, description, last, primitive, created_by, type, version) VALUES ('1ba494f9-387e-44a9-b211-8008299d7773', '2025-02-16 18:13:23.066000 +00:00', 'type_1', true, true, 'system', 'type_1', 't1-v1');
INSERT INTO public.type_definition (id, created_at, description, last, primitive, created_by, type, version) VALUES ('94c220f1-7576-4d3b-96ff-6128be479f34', '2025-02-16 18:13:23.066000 +00:00', 'type_2', true, true, 'system', 'type_2', 't2-v1');
INSERT INTO public.type_definition (id, created_at, description, last, primitive, created_by, type, version) VALUES ('54aa4cd9-d12a-4015-886d-70c40fd0049b', '2025-02-16 18:13:23.066000 +00:00', 'type_3', true, true, 'system', 'type_3', 't3-v1');
INSERT INTO public.type_definition (id, created_at, description, last, primitive, created_by, type, version) VALUES ('d4cedd54-6423-45a6-86ca-821eae9b3573', '2025-02-16 18:13:23.066000 +00:00', 'type_2', true, true, 'system', 'type_2', 't2-v2');
INSERT INTO public.type_definition (id, created_at, description, last, primitive, created_by, type, version) VALUES ('cd569d16-1f50-4cd1-85eb-74a763c98b5d', '2025-02-16 18:13:23.066000 +00:00', 'type_3', true, true, 'system', 'type_3', 't3-v2');

INSERT INTO public.attribute (attribute_type, name, description, nullable, value_validators, value_type, file_extensions, key_type, key_validators, type_definition, reference_type) VALUES ('Value', 'common_attr', 'Common Attribute in Root', true, null, null, null, null, null, '1ba494f9-387e-44a9-b211-8008299d7773', null);
INSERT INTO public.attribute (attribute_type, name, description, nullable, value_validators, value_type, file_extensions, key_type, key_validators, type_definition, reference_type) VALUES ('Value', 'common_attr', 'Overwritten Attribute in Child 1', true, null, null, null, null, null, '94c220f1-7576-4d3b-96ff-6128be479f34', null);
INSERT INTO public.attribute (attribute_type, name, description, nullable, value_validators, value_type, file_extensions, key_type, key_validators, type_definition, reference_type) VALUES ('Array', 'unique_attr', 'Unique Attribute in Child 1', false, null, 'Value', null, null, null, '94c220f1-7576-4d3b-96ff-6128be479f34', null);
INSERT INTO public.attribute (attribute_type, name, description, nullable, value_validators, value_type, file_extensions, key_type, key_validators, type_definition, reference_type) VALUES ('Document', 'exclusive_attr', 'Exclusive Attribute in Child 2', false, null, null, null, null, null, '54aa4cd9-d12a-4015-886d-70c40fd0049b', 'type_1');
INSERT INTO public.attribute (attribute_type, name, description, nullable, value_validators, value_type, file_extensions, key_type, key_validators, type_definition, reference_type) VALUES ('Value', 'unique_attr', 'Changed Attribute in Child 1', false, null, 'Value', null, null, null, 'd4cedd54-6423-45a6-86ca-821eae9b3573', null);
INSERT INTO public.attribute (attribute_type, name, description, nullable, value_validators, value_type, file_extensions, key_type, key_validators, type_definition, reference_type) VALUES ('Document', 'exclusive_attr', 'Exclusive Attribute in Child 2', false, null, null, null, null, null, 'cd569d16-1f50-4cd1-85eb-74a763c98b5d', 'type_1');
INSERT INTO public.attribute (attribute_type, name, description, nullable, value_validators, value_type, file_extensions, key_type, key_validators, type_definition, reference_type) VALUES ('Map', 'exclusive_attr2', 'Added Attribute in Child 2', false, null, 'Value', null, 'Value', null, 'cd569d16-1f50-4cd1-85eb-74a763c98b5d', 'type_1');
INSERT INTO public.attribute (attribute_type, name, description, nullable, value_validators, value_type, file_extensions, key_type, key_validators, type_definition, reference_type) VALUES ('Value', 'common_attr', 'Overwritten Attribute in Child 2', true, null, null, null, null, null, 'cd569d16-1f50-4cd1-85eb-74a763c98b5d', null);

-- Type1: 그대로
-- Type2: 속성 2개(그 중 1개 상속) -1999-> 상속 안 하고 1개 값 변경
-- Type3: 속성 1개 -2005-> 속성 3개(그 중 1개 상속, 기존 속성 변경)