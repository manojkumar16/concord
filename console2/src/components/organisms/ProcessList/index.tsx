import * as React from 'react';
import { connect, Dispatch } from 'react-redux';
import { Link } from 'react-router-dom';
import { Button, Menu, Table, Loader } from 'semantic-ui-react';

import { RequestError } from '../../../api/common';
import { ProcessEntry } from '../../../api/process';
import { actions, Processes, State } from '../../../state/data/processes';
import { LocalTimestamp, ProcessStatusIcon, RequestErrorMessage } from '../../molecules';

interface StateProps {
    processes: ProcessEntry[];
    loading: boolean;
    error: RequestError;
}

interface DispatchProps {
    load: (orgName?: string, projectName?: string) => void;
}

interface ExternalProps {
    orgName?: string;
    projectName?: string;
}

const renderProcessLink = (row: ProcessEntry) => {
    const { instanceId } = row;
    return <Link to={`/process/${instanceId}`}>{instanceId}</Link>;
};

const renderTableRow = (row: ProcessEntry, addProjectColumn: boolean) => {
    return (
        <Table.Row key={row.instanceId}>
            <Table.Cell textAlign="center">
                <ProcessStatusIcon status={row.status} />
            </Table.Cell>
            <Table.Cell>{renderProcessLink(row)}</Table.Cell>
            {addProjectColumn && (
                <Table.Cell>
                    <Link to={`/org/${row.orgName}/project/${row.projectName}`}>
                        {row.projectName}
                    </Link>
                </Table.Cell>
            )}
            <Table.Cell>{row.initiator}</Table.Cell>
            <Table.Cell>
                <LocalTimestamp value={row.createdAt} />
            </Table.Cell>
            <Table.Cell>
                <LocalTimestamp value={row.lastUpdatedAt} />
            </Table.Cell>
        </Table.Row>
    );
};

class ProcessList extends React.PureComponent<StateProps & DispatchProps & ExternalProps> {
    componentWillMount() {
        this.update();
    }

    componentDidUpdate(prevProps: ExternalProps) {
        const { orgName: newOrgName, projectName: newProjectName } = this.props;
        const { orgName: oldOrgName, projectName: oldProjectName } = prevProps;

        if (newOrgName !== oldOrgName || newProjectName !== oldProjectName) {
            this.update();
        }
    }

    update() {
        const { orgName, projectName, load } = this.props;
        load(orgName, projectName);
    }

    renderTable() {
        const { projectName, processes, loading } = this.props;

        if (loading) {
            return <Loader active={loading} />;
        }

        if (!processes) {
            return <p>No processes found.</p>;
        }

        const addProjectColumn = !projectName;

        return (
            <Table celled={true} attached="bottom">
                <Table.Header>
                    <Table.Row>
                        <Table.HeaderCell collapsing={true}>Status</Table.HeaderCell>
                        <Table.HeaderCell>Instance ID</Table.HeaderCell>

                        {addProjectColumn && <Table.HeaderCell>Project</Table.HeaderCell>}

                        <Table.HeaderCell collapsing={true}>Initiator</Table.HeaderCell>
                        <Table.HeaderCell>Created</Table.HeaderCell>
                        <Table.HeaderCell>Updated</Table.HeaderCell>
                    </Table.Row>
                </Table.Header>

                <Table.Body>{processes.map((p) => renderTableRow(p, addProjectColumn))}</Table.Body>
            </Table>
        );
    }

    render() {
        const { loading, error } = this.props;

        if (error) {
            return <RequestErrorMessage error={error} />;
        }

        return (
            <>
                <Menu attached="top" borderless={true}>
                    <Menu.Item>
                        <Button
                            basic={true}
                            icon="refresh"
                            loading={loading}
                            onClick={() => this.update()}
                        />
                    </Menu.Item>
                </Menu>

                {this.renderTable()}
            </>
        );
    }
}

// TODO move to selectors
const makeProcessList = (data: Processes): ProcessEntry[] => {
    return Object.keys(data)
        .map((k) => data[k])
        .sort((a, b) => (a.createdAt < b.createdAt ? 1 : a.createdAt > b.createdAt ? -1 : 0));
};

const mapStateToProps = ({ processes }: { processes: State }): StateProps => ({
    loading: processes.loading,
    error: processes.error,
    processes: makeProcessList(processes.processById)
});

const mapDispatchToProps = (dispatch: Dispatch<{}>): DispatchProps => ({
    load: (orgName?, projectName?) => dispatch(actions.listProjectProcesses(orgName, projectName))
});

export default connect(mapStateToProps, mapDispatchToProps)(ProcessList);
