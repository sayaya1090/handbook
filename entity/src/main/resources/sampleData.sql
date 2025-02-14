INSERT INTO public."user" (id, last_modified_at, created_at, last_login_at, name) VALUES ('system', NOW(), NOW(), null, 'system');

INSERT INTO public.type (id, last_modified_at, created_at, description, created_by, last_modified_by, parent) VALUES ('type_1', NOW(), NOW(), 'type_1', 'system', 'system', null);
INSERT INTO public.type (id, last_modified_at, created_at, description, created_by, last_modified_by, parent) VALUES ('type_2', NOW(), NOW(), 'type_2', 'system', 'system', 'type_1');
INSERT INTO public.type (id, last_modified_at, created_at, description, created_by, last_modified_by, parent) VALUES ('type_3', NOW(), NOW(), 'type_3', 'system', 'system', 'type_2');

INSERT INTO public.attribute (attribute_type, name, type, description, nullable, primitive, value_validators, file_extensions, key_type, key_validators, value_type, reference_type) VALUES ('Value', 'common_attr', 'type_2', 'Overwritten Attribute in Child 1', true, true, null, null, null, null, null, null);
INSERT INTO public.attribute (attribute_type, name, type, description, nullable, primitive, value_validators, file_extensions, key_type, key_validators, value_type, reference_type) VALUES ('Value', 'common_attr', 'type_1', 'Common Attribute in Root', true, false, null, null, null, null, null, null);
INSERT INTO public.attribute (attribute_type, name, type, description, nullable, primitive, value_validators, file_extensions, key_type, key_validators, value_type, reference_type) VALUES ('Array', 'unique_attr', 'type_2', 'Unique Attribute in Child 1', false, true, null, null, null, null, 'Value', null);
INSERT INTO public.attribute (attribute_type, name, type, description, nullable, primitive, value_validators, file_extensions, key_type, key_validators, value_type, reference_type) VALUES ('Document', 'exclusive_attr', 'type_3', 'Exclusive Attribute in Child 2', false, false, null, null, null, null, null, 'type_1');


