import * as React from 'react';
import { Header, Input, Menu, Modal } from 'semantic-ui-react';

import { AnsibleEvent, AnsibleStatus, ProcessEventEntry } from '../../../api/process/event';
import { AnsibleTaskList } from '../index';

export interface AnsibleHostListEntry {
    host: string;
    status: AnsibleStatus;
}

interface State {
    filter?: string;
}

interface Props {
    hosts: AnsibleHostListEntry[];
    hostEventsFn: (host: string) => Array<ProcessEventEntry<AnsibleEvent>>;
    selectedStatus?: AnsibleStatus;
}

const applyFilter = (
    hosts: AnsibleHostListEntry[],
    selectedStatus?: AnsibleStatus,
    filter?: string
): string[] => {
    let result = [...hosts];

    if (selectedStatus) {
        result = result.filter(({ status }) => status === selectedStatus);
    }

    if (filter) {
        result = result.filter(({ host }) => host.toLowerCase().search(filter) >= 0);
    }

    return Array.from(new Set(result.map((e) => e.host))).sort();
};

class AnsibleHostList extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = {};
    }

    renderHostItem(host: string, idx: number) {
        const { hostEventsFn } = this.props;
        return (
            <Modal
                key={idx}
                basic={true}
                size="fullscreen"
                dimmer="inverted"
                trigger={
                    <Menu.Item key={idx} name={host}>
                        {host}
                    </Menu.Item>
                }>
                <Modal.Content>
                    <AnsibleTaskList title={host} events={hostEventsFn(host)} />
                </Modal.Content>
            </Modal>
        );
    }

    render() {
        const { hosts, selectedStatus } = this.props;
        const { filter } = this.state;

        return (
            <>
                <Menu attached="top" borderless={true} secondary={true}>
                    <Menu.Item>
                        <Header as="h4">Hosts by Status {selectedStatus}</Header>
                    </Menu.Item>
                    <Menu.Item position="right">
                        <Input
                            type="text"
                            icon="filter"
                            size="small"
                            placeholder="Search"
                            onChange={(e, { value }) => this.setState({ filter: value })}
                        />
                    </Menu.Item>
                </Menu>
                <Menu attached="bottom" vertical={true} fluid={true}>
                    {applyFilter(hosts, selectedStatus, filter).map((host, idx) =>
                        this.renderHostItem(host, idx)
                    )}
                </Menu>
            </>
        );
    }
}

export default AnsibleHostList;
