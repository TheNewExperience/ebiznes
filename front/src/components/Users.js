import React,{Component} from 'react'


class Users extends Component{

    constructor() {
        super();
        this.state={
            users: [],
        };
    }

    componentDidMount() {
        var url = "http://localhost:8080/usersjson"


            fetch(url, {
                mode: 'cors',
                headers:{
                    'Accept': 'application/json',
                    'Content-Type': 'application/json',
                    'Access-Control-Allow-Origin':'http://localhost:3000',
                },
                method: 'GET',
            })
                .then(results => {
                    return results.json();
                }).then(data => {
                let users = data.map((us) => {
                    return (
                        <div key={us.id_user}>
                            <div classEmail="email">{us.email}</div>
                        </div>
                    )
                })
                this.setState({users: users})
            })
        }

    render() {
      return (
          <div className="users">
              {this.state.users}
          </div>
      )
    }
}

export default Users;