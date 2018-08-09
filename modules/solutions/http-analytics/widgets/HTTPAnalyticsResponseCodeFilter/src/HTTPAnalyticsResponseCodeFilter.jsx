/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

import React from 'react';
import PropTypes from 'prop-types';
import Widget from '@wso2-dashboards/widget';
import {MuiThemeProvider, createMuiTheme} from '@material-ui/core/styles';
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';
import MenuItem from '@material-ui/core/MenuItem';
import ArrowDropDownIcon from '@material-ui/icons/ArrowDropDown';
import CancelIcon from '@material-ui/icons/Cancel';
import ArrowDropUpIcon from '@material-ui/icons/ArrowDropUp';
import ClearIcon from '@material-ui/icons/Clear';
import Chip from '@material-ui/core/Chip';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Select from 'react-select';
import {Scrollbars} from 'react-custom-scrollbars';
import JssProvider from 'react-jss/lib/JssProvider';

const darkTheme = createMuiTheme({
    palette: {
        type: "dark"
    }
});

const lightTheme = createMuiTheme({
    palette: {
        type: "light"
    }
});

const customStyles = {
    input: () => ({
        color: 'white'
    }),
    multiValue: () => ({
        borderRadius: 15,
        display: 'flex',
        flexWrap: 'wrap',
        color: 'black',
        fontSize: '90%',
        overflow: 'hidden',
        paddingLeft: 6,
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
        backgroundColor: 'darkgrey',
        minWidth: '20'
    }),
    singleValue: () => ({
        display: 'flex',
        flexWrap: 'wrap',
        color: 'white',
        fontSize: '95%',
    }),
    control: () => ({
        height: 10,
        borderRadius: 5,
        alignItems: 'center',
        minHeight: 30,
        backgroundColor: 'rgb(51, 51, 51)',
        borderColor: 'grey',
        borderStyle: 'solid',
        borderWidth: 0,
        boxShadow: '0 0 0 1px grey',
        cursor: 'default',
        display: 'flex',
        flexWrap: 'wrap',
        justifyContent: 'space-between',
        outline: '0 !important',
        position: 'relative',
        transition: 'all 100ms',
        paddingTop: 2
    }),
    option: (styles, {data, isDisabled, isFocused}) => {
        return {
            ...styles,
            height: 30,
            backgroundColor: isDisabled
                ? null
                : isFocused ? 'rgba(255, 255, 255, 0.1)' : null,
        };
    },

    menuList: () => ({
        backgroundColor: 'rgb(51, 51, 51)',
    }),
};

/**
 * Options class passed to the react-select component
 */
class Option extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        const {children, isFocused, onFocus, isDisabled} = this.props;
        return (
            <MenuItem
                onFocus={onFocus}
                selected={isFocused}
                disabled={isDisabled}
                onClick={() => this.props.onSelect(this.props.option, event)}
                component="div"
            >
                {children}
            </MenuItem>
        );
    }
}

/**
 * Function to wrap react-select component
 * @param props
 * @returns <Select> componet
 * @constructor
 */
function SelectWrapped(props) {
    const {classes, ...other} = props;
    return (
        <Select
            styles={props.muiTheme.name === 'dark' ? customStyles : {}}
            optionComponent={Option}
            noResultsText={<div>{'No results found'}</div>}
            arrowRenderer={arrowProps => {
                return arrowProps.isOpen ? <ArrowDropUpIcon/> : <ArrowDropDownIcon/>;
            }}
            clearRenderer={() => <ClearIcon/>}
            valueComponent={valueProps => {
                const {value, children, onRemove} = valueProps;
                const onDelete = event => {
                    event.preventDefault();
                    event.stopPropagation();
                    onRemove(value);
                };
                if (onRemove) {
                    return (
                        <Chip
                            tabIndex={-1}
                            label={children}
                            className={classes.chip}
                            deleteIcon={<CancelIcon onTouchEnd={onDelete}/>}
                            onDelete={onDelete}
                        />
                    );
                }
                return <div className="Select-value">{children}</div>;
            }}
            {...other}
        />
    );
}

/**
 * HTTPAnalyticsResponseCodeFilter which renders the perspective and filter in response code analytics page
 */
class HTTPAnalyticsResponseCodeFilter extends Widget {
    constructor(props) {
        super(props);
        this.state = {
            width: this.props.glContainer.width,
            height: this.props.glContainer.height,
            perspective: 3,
            services: [],
            serviceOptions: [],
            selectedServiceValues: null,
            faultyProviderConf: false
        };
        this.props.glContainer.on('resize', () =>
            this.setState({
                width: this.props.glContainer.width,
                height: this.props.glContainer.height
            })
        );
        this.handleChange = this.handleChange.bind(this);
        this.handleDataReceived = this.handleDataReceived.bind(this);
        this.publishUpdate = this.publishUpdate.bind(this);
    }

    /**
     * Publish user selection to other widgets
     */
    publishUpdate() {
        let filterOptions = {
            perspective: this.state.perspective,
            selectedServiceValues: this.state.selectedServiceValues,
        };
        super.publish(filterOptions);
    }

    /**
     * Set the state of the widget after metadata and data is received from SiddhiAppProvider
     * @param data
     */
    handleDataReceived(data) {
        let services = [], serviceOptions;
        data.data.map(dataUnit => {
            services.push(dataUnit[1]);
        });

        services = services.filter((v, i, a) => a.indexOf(v) === i);
        serviceOptions = services.map(service => ({
            value: service,
            label: service,
            disabled: false
        }));

        this.setState({
            services, serviceOptions
        }, this.publishUpdate);
    }

    /**
     * Publish user selection in filters
     * @param values
     */
    handleChange(values){
        let updatedOptions = this.state.services.map(option => ({
            value: option,
            label: option,
            disabled: false
        }));
        this.setState({
            selectedServiceValues: values,
            serviceOptions: updatedOptions
        }, this.publishUpdate);
    };

    componentDidMount() {
        super.getWidgetConfiguration(this.props.widgetID)
            .then((message) => {
                super.getWidgetChannelManager()
                    .subscribeWidget(this.props.id, this.handleDataReceived, message.data.configs.providerConfig);
            })
            .catch((error) => {
                this.setState({
                    faultyProviderConf: true
                });
            });
    }

    componentWillUnmount() {
        super.getWidgetChannelManager().unsubscribeWidget(this.props.id);
    }

    render() {
        const {classes} = this.props;
        return (
            <JssProvider generateClassName={generateClassName}>
                <MuiThemeProvider theme={this.props.muiTheme.name === 'dark' ? darkTheme : lightTheme}>
                    <Scrollbars style={{height: this.state.height}}>
                        <div style={{paddingLeft: 24, paddingRight: 16}}>
                            <Tabs
                                value={this.state.perspective}
                                onChange={(evt, value) => this.setState({perspective: value}, this.publishUpdate)}>
                                <Tab value={3} label="Response Code"/>
                            </Tabs>
                            <TextField
                                    fullWidth
                                    value={this.state.selectedServiceValues}
                                    onChange={this.handleChange}
                                    placeholder="Filter by Service"
                                    label=""
                                    InputLabelProps={{
                                        shrink: false,
                                    }}
                                    InputProps={{
                                        inputComponent: SelectWrapped,
                                        inputProps: {
                                            classes,
                                            isMulti: false,
                                            simpleValue: true,
                                            options: this.state.serviceOptions,
                                            muiTheme: this.props.muiTheme,
                                        }
                                    }}
                                />
                            </div>
                    </Scrollbars>
                </MuiThemeProvider>
            </JssProvider>
        );
    }
}

//This is the workaround suggested in https://github.com/marmelab/react-admin/issues/1782
const escapeRegex = /([[\].#*$><+~=|^:(),"'`\s])/g;
let classCounter = 0;

export const generateClassName = (rule, styleSheet) => {
    classCounter += 1;

    if (process.env.NODE_ENV === 'production') {
        return `c${classCounter}`;
    }

    if (styleSheet && styleSheet.options.classNamePrefix) {
        let prefix = styleSheet.options.classNamePrefix;
        // Sanitize the string as will be used to prefix the generated class name.
        prefix = prefix.replace(escapeRegex, '-');

        if (prefix.match(/^Mui/)) {
            return `${prefix}-${rule.key}`;
        }

        return `${prefix}-${rule.key}-${classCounter}`;
    }

    return `${rule.key}-${classCounter}`;
};

HTTPAnalyticsResponseCodeFilter.propTypes = {
    classes: PropTypes.object.isRequired,
};
global.dashboard.registerWidget("HTTPAnalyticsResponseCodeFilter", HTTPAnalyticsResponseCodeFilter);
