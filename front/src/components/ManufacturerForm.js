import React,{Component} from 'react';

class ManufacturerForm extends Component{

    constructor() {
        super();
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleSubmit(event) {
        event.preventDefault();
        const data = new FormData(event.target);

        var url = 'http://localhost:8080/addmanufacturerjson'

        fetch(url, {
            method: 'POST',
            body: data,
        });
    }

    render() {
     return (
         <form onSubmit={this.handleSubmit}>
             <label htmlFor="name">Manufacturer name</label>
             <input id="name" name="name" type="text"/>

             <button>Add manufacturer</button>
         </form>
     );
    }
}

export default ManufacturerForm;
