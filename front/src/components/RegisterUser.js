import React,{Component} from 'react'


class RegisterUser extends Component{

    constructor() {
        super();
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleSubmit(event) {
        event.preventDefault();
        const data = new FormData(event.target);

        var url = 'http://localhost:8080/adduserjson'

        fetch(url, {
            method: 'POST',

            body: data,
        });
    }

    render() {


        return (
            <form onSubmit={this.handleSubmit}>
                <label htmlFor="text">User Email</label>
                <input id="email" name="email" type="text"/>
                <input id="password" name="password" type="text"/>
                <input id="reset_question" name="reset_question" type="text"/>
                <input id="reset_answer" name="reset_answer" type="text"/>

                <button>Register user</button>
            </form>
        );
    }
}

export default RegisterUser;
