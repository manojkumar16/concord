/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 - 2018 Walmart Inc.
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

import { Field, getIn } from 'formik';
import { FieldProps } from 'formik/dist/Field';
import * as React from 'react';
import { Form, FormInputProps, Input, Label } from 'semantic-ui-react';

export default class extends React.Component<FormInputProps> {
    render() {
        const { name: fieldName, label, required, ...rest } = this.props;

        return (
            <Field
                name={fieldName}
                render={({ field, form }: FieldProps) => {
                    const touched = getIn(form.touched, fieldName);
                    const error = getIn(form.errors, fieldName);
                    const invalid = !!(touched && error);

                    const handleChange = (ev: any) =>
                        form.setFieldValue(fieldName, ev.target.files[0]);

                    return (
                        <Form.Field error={invalid} required={required}>
                            <label>{label}</label>
                            <Input {...rest} type="file" onChange={handleChange} />
                            {invalid && error && (
                                <Label basic={true} pointing={true} color="red">
                                    {error}
                                </Label>
                            )}
                        </Form.Field>
                    );
                }}
            />
        );
    }
}
