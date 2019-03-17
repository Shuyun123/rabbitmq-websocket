import React, { Component } from 'react';
import PropTypes from 'prop-types';
import './App.css';

import Cookies from 'universal-cookie';

import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

class App extends Component {

    static propTypes = {
        cookies: PropTypes.any
    };

    static defaultProps = {
        cookies: new Cookies()
    };

    constructor(props) {
        super(props);

        this.state = {
            connected: false,
            token: 'lu6ic1nvejtfrvnv9avpcl2ag9',
            pMessages: []
        };

        this.updateToken = this.updateToken.bind(this);
        this.connect = this.connect.bind(this);
        this.disconnect = this.disconnect.bind(this);
        this.sendMessage = this.sendMessage.bind(this);
    }

    sendMessage(){
        const client = this.state.wsClient;
        const connected = this.state.connected;
        if(connected && client != null){
            client.send("/message/client", {}, JSON.stringify({ 'name': 'anumbrella' }));
        }
    }   


    connect() {
        this.updateToken();
        let client = Stomp.over(new SockJS('http://localhost:8080/ws?user=211'));
        client.heartbeat.outgoing = 0;
        client.heartbeat.incoming = 0;

        this.setState(() => ({
            wsClient: client
        }));

        client.connect({
            name: '211',
        }, (frame) => {
            this.setState(() => ({
                connected: true
            }));
 
             client.subscribe('/user/topic/reply', (msg) => {
                
                const messages = this.state.pMessages;
                msg.id = Date.now();
                const newMessages = messages.concat([msg]);
                
                console.log("user");
                
                console.log(msg);

                this.setState(() => ({
                    pMessages: newMessages
                }));
            });

            client.subscribe('/topic/notice', (msg) => {
                
                const messages = this.state.pMessages;
                msg.id = Date.now();
                const newMessages = messages.concat([msg]);
                console.log("notice");
                console.log(msg);

                this.setState(() => ({
                    pMessages: newMessages
                }));
            });

        });
    }

    disconnect() {
        const client = this.state.wsClient;

        if (client !== null) {
            client.disconnect();

            this.setState(() => ({
                connected: false
            }));
        }
    }

    updateToken() {
        const c = this.props.cookies;
        c.set('t', this.state.token, { path: '/' });
    }

    render() {
        return (
            <div className="container">
                <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
                    <span className="navbar-brand">SockJS Client</span>
                </nav>

                <div className="d-flex flex-column">
                    <div className="row mt-3">
                        <div className="btn-group ml-2" role="group">
                            <button
                                type="button"
                                className="btn btn-success"
                                onClick={this.connect}
                                disabled={this.state.connected}>Connect
                            </button>
                            <button
                                type="button"
                                className="btn btn-dark"
                                onClick={this.disconnect}
                                disabled={!this.state.connected}>Disconnect
                            </button>
                        </div>

                        <button
                            type="button"
                            className="btn btn-default ml-2"
                            onClick={this.sendMessage}
                            disabled={!this.state.connected}>Send
                        </button>
                    </div>

                    <div className="row mt-4">
                        <ul className="list-group">
                            {this.state.pMessages.map((m) => {
                                return (
                                    <li className="list-group-item" key={m.id}>{m.body}</li>
                                );
                            })}
                        </ul>
                    </div>
                </div>
            </div>
        );
    }
}

export default App;
