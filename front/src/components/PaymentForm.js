import React,{Component} from 'react';

class PaymentForm extends Component{

    constructor() {
        super();
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleSubmit(event) {
        event.preventDefault();
        const data = new FormData(event.target);

        var url = 'http://localhost:8080/addpaymentjson'

        fetch(url, {
            method: 'POST',
            body: data,
        });
    }

    render() {
     return (
         <form onSubmit={this.handleSubmit}>
             <label htmlFor="name">Payment method</label>
             <input id="name" name="name" type="text"/>

             <button>Add payment method</button>
         </form>
     );
    }
}

export default PaymentForm;
