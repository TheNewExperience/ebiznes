import React,{Component} from 'react'


class Payments extends Component{

    constructor() {
        super();
        this.state={
            payments: [],
        };
    }

    componentDidMount() {
        var url = "http://localhost:8080/paymentsjson"


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
                let payments = data.map((pay) => {
                    return (
                        <div key={pay.id_payment_method}>
                            <div className="name">{pay.name}</div>
                        </div>
                    )
                })
                this.setState({payments: payments})
            })
        }

    render() {
      return (
          <div className="payments">
              {this.state.payments}
          </div>
      )
    }
}

export default Payments;