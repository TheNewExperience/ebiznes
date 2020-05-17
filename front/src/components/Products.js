import React,{Component} from 'react'


class Products extends Component{

    constructor() {
        super();
        this.state={
            products: [],
        };
    }

    componentDidMount() {
        var url = "http://localhost:8080/productsjson"


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
                let products = data.map((prod) => {
                    return (
                        <div key={prod.id_product}>
                            <div className="product_name">{prod.product_name}</div>
                            <div classDescription="product_description">{prod.product_description}</div>
                            <div classPrice="product_price">{prod.product_price}</div>
                            <div classCategory="product_category">{prod.category}</div>
                            <div classDescription="product_manufacturer">{prod.manufacturer}</div>

                        </div>
                    )
                })
                this.setState({products: products})
            })
        }

    render() {
      return (
          <div className="products">
              {this.state.products}
          </div>
      )
    }
}

export default Products;