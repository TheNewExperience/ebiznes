import React,{Component} from 'react';

class Category extends Component{

    constructor() {
        super();
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    handleSubmit(event) {
        event.preventDefault();
        const data = new FormData(event.target);

        var url = 'http://localhost:8080/addcategoryjson'

        fetch(url, {
            method: 'POST',
            body: data,
        });
    }

    render() {
     return (
         <form onSubmit={this.handleSubmit}>
             <label htmlFor="name">Category name</label>
             <input id="name" name="name" type="text"/>

             <button>Add category</button>
         </form>
     );
    }
}

export default Category;
